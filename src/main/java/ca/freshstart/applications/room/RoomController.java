package ca.freshstart.applications.room;

import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventRepository;
import ca.freshstart.types.AbstractController;
import ca.freshstart.data.restriction.entity.Restriction;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.room.entity.RoomBookedTime;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import ca.freshstart.data.restriction.repository.RestrictionRepository;
import ca.freshstart.data.room.repository.RoomRepository;
import ca.freshstart.types.Constants;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.helpers.ModelValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class RoomController extends AbstractController {
    private final RoomRepository roomRepository;
    private final RestrictionRepository restrictionRepository;
    private final ConcreteEventRepository concreteEventRepository;
    private final ConcreteCalendarEventRepository concreteCalendarEventRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Return list of rooms
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested rooms
     */
    @RequestMapping(value = "/rooms", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Collection<Room> rooms(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                  @RequestParam(value = "sort", required = false) String sort) {
        return roomRepository.findAllAsList(new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort)));
    }

    /**
     * Return count of rooms
     *
     * @return Count of rooms
     */
    @RequestMapping(value = "/rooms/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count roomsCount() {
        return new Count(roomRepository.count());
    }

    /**
     * Add new room
     *
     * @param room room to add
     * @return Id of the room
     */
    @RequestMapping(value = "/room", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public IdResponse addRoom(@RequestBody Room room) {
        ModelValidation.validateRoom(room);

        room = saveOrReplaceArchived(room);
        return new IdResponse(room.getId());
    }

    private Room saveOrReplaceArchived(Room room) {
        try {
            return roomRepository.save(room);
        } catch (DataIntegrityViolationException ex) {
            Room foundRoom = roomRepository.findArchivedByName(room.getName())
                    .orElseThrow(() -> new NotFoundException(String.format("Room with the name (%s) already exist", room.getName())));

            foundRoom.setName(room.getName());
            foundRoom.setCapacity(room.getCapacity());
            foundRoom.setArchived(false);

            return roomRepository.save(foundRoom);
        }
    }

    /**
     * Return information about one room
     *
     * @param roomId id of room
     * @return Room data
     */
    @RequestMapping(value = "/room/{roomId}", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Room getById(@PathVariable("roomId") Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("No such room"));
    }

    /**
     * Update room
     *
     * @param roomId   id of room
     * @param roomFrom Updated room
     */
    @RequestMapping(value = "/room/{roomId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void updateRoom(@PathVariable("roomId") Long roomId, @RequestBody Room roomFrom) {
        ModelValidation.validateRoom(roomFrom);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("No such room"));

        room.setName(roomFrom.getName());
        room.setCapacity(roomFrom.getCapacity());

        saveOrReplaceArchived(room);
    }

    /**
     * Delete room
     *
     * @param roomId id of room
     */
    @RequestMapping(value = "/room/{roomId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SETTINGS")
    public void deleteRoomById(@PathVariable("roomId") Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("No such room"));

        List<Restriction> restrictions = restrictionRepository.findByRoomId(roomId);

        restrictions.forEach(restriction -> {
            Set<Room> toRemove = restriction.getRooms()
                    .stream()
                    .filter(r -> r.getId().equals(roomId))
                    .collect(Collectors.toSet());
            restriction.getRooms().removeAll(toRemove);
            restrictionRepository.save(restriction);
        });

        try {
            roomRepository.delete(room);
        } catch (Exception e) {
            room.setArchived(true);
            roomRepository.save(room);
        }
    }

    /**
     * Return list of rooms for range of dates with booked time
     *
     * @param sessionId id of session
     * @param dateFrom  start of date range to get booked items for (format 'DD-MM-YYYY')
     * @param dateTo    end of date range to get booked items (format 'DD-MM-YYYY')
     * @return Requested rooms
     */
    @RequestMapping(value = "/rooms/session/{sessionId}/booked", method = RequestMethod.GET)
    @Secured({"ROLE_SUGGESTED_SERVICES", "ROLE_SUGGESTED_SERVICES_UI"})
    public Collection<RoomBookedTime> calendarEventsConcrete(@PathVariable("sessionId") Long sessionId,
                                                             @RequestParam("dateFrom") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateFrom,
                                                             @RequestParam("dateTo") @DateTimeFormat(pattern = Constants.DATE_FORMAT) Date dateTo) {

        List<ConcreteEvent> c_events = concreteEventRepository.findByDateInRange(dateFrom, dateTo);

        String select_calendar_events = "SELECT (SUM(e.time_clean) + SUM (e.time_prep) + SUM(e.time_processing)) as booked, room_id, date FROM concrete_calendar_event e WHERE e.archived = false AND room_id NOTNULL AND e.date >= ? AND e.date <= ? GROUP BY e.room_id,e.date";
        String select_events = "SELECT (SUM(e.time_clean) + SUM (e.time_prep) + SUM(e.time_processing)) as booked, room_id, date FROM concrete_event e WHERE room_id NOTNULL AND e.date >= ? AND e.date <= ? GROUP BY e.room_id,e.date";
        List<RoomBookedTime> m = jdbcTemplate.query(select_calendar_events,
                new Object[]{dateFrom, dateTo},
                BeanPropertyRowMapper.newInstance(RoomBookedTime.class)
        );
        List<RoomBookedTime> m2 = jdbcTemplate.query(select_events,
                new Object[]{dateFrom, dateTo},
                BeanPropertyRowMapper.newInstance(RoomBookedTime.class)
        );

        return joinLists(m, m2);
    }

    private static List<RoomBookedTime> joinLists(List<RoomBookedTime> a, List<RoomBookedTime> b) {
        if ((a == null) || (a.isEmpty() && (b != null))) return b;
        if ((b == null) || b.isEmpty()) return a;
        ArrayList<RoomBookedTime> result = new ArrayList<RoomBookedTime>(a.size() + b.size()); // Закладываем размер достаточный для всех элементов
        result.addAll(a);
        result.addAll(b);
        return result;
    }
}