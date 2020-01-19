package ca.freshstart.data.report.services;

//Entities

import ca.freshstart.data.client.entity.Client;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.data.concreteCalendarEvent.entity.ConcreteCalendarEvent;
import ca.freshstart.data.concreteCalendarEvent.repository.ConcreteCalendarEventRepository;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.concreteEvent.repositories.ConcreteEventRepository;
import ca.freshstart.data.report.entity.Report;

//Annotations
import ca.freshstart.data.report.entity.ReportTableItem;
import ca.freshstart.data.report.repository.ReportRepository;
import ca.freshstart.data.room.entity.Room;
import ca.freshstart.data.therapist.entity.Therapist;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.helpers.CspUtils;
import ca.freshstart.types.Count;
import ca.freshstart.types.IdResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("reportService")
public class ReportService {
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ConcreteEventRepository concreteEventRepository;
    @Autowired
    private ConcreteCalendarEventRepository concreteCalendarEventRepository;


    /**
     * @param {PageRequest} page
     * @return {List <Report>}
     */
    public List<Report> getList(PageRequest page) {
        return reportRepository.findAllAsList(page);
    }

    public Count getCount() {
        return new Count(reportRepository.count());
    }

    public Optional<Report> findByKeyHash(String reportKey) {
        return reportRepository.findByUrl(reportKey);
    }

    public Optional<Report> findById(Long reportId) {
        return reportRepository.findById(reportId);
    }

    public void createReport(List<Report> reports) {
        reports = reports
                .stream()
                .peek((Report report) -> {
                    report.setUrl(UUID.randomUUID().toString());
                })
                .collect(Collectors.toList());

        saveReport(reports);
    }

    public void saveReport(Report report){
        reportRepository.save(report);
    }

    public void saveReport(List<Report> reports) {
        reportRepository.save(reports);
    }

    public List<ReportTableItem> getReportByClient(Report report) {
        Client client = report.getClient();

        Date dateTo = (report.getDateTo() != null)
                ? report.getDateTo()
                : report.getDateFrom();

        List<ReportTableItem> tableItems = getReportTableItems(report.getDateFrom(), dateTo)
                .stream()
                .filter((ReportTableItem tableItem) ->
                   tableItem.getClient().getId().equals(client.getId())
                )
                .collect(Collectors.toList());

        return tableItems;
    }

    public List<ReportTableItem> getReportByRoom(Report report) {
        Room room = report.getRoom();

        Date dateTo = (report.getDateTo() != null)
                ? report.getDateTo()
                : report.getDateFrom();

        List<ReportTableItem> tableItems = getReportTableItems(report.getDateFrom(), dateTo)
                .stream()
                .filter((ReportTableItem tableItem) ->
                        tableItem.getRoom().getId().equals(room.getId())
                )
                .collect(Collectors.toList());

        return tableItems;
    }

    public List<ReportTableItem> getReportByTherapist(Report report) {
        Therapist therapist = report.getTherapist();

        Date dateTo = (report.getDateTo() != null)
                ? report.getDateTo()
                : report.getDateFrom();

        List<ReportTableItem> tableItems = getReportTableItems(report.getDateFrom(), dateTo)
                .stream()
                .filter((ReportTableItem tableItem) ->
                        tableItem.getTherapist().getId().equals(therapist.getId())
                )
                .collect(Collectors.toList());

        return tableItems;
    }

    public List<ReportTableItem> getReportTableItems(Date dateFrom, Date dateTo) {
        List<ReportTableItem> tableItems = new ArrayList<ReportTableItem>();

        concreteCalendarEventRepository.findAllInRange(dateFrom, dateTo)
                .forEach((ConcreteCalendarEvent event) -> {
                    event.getClients().forEach((Client client) -> {
                        ReportTableItem table = new ReportTableItem();
                        table.setDate(event.getDate());
                        table.setServiceName(event.getEvent().getName());
                        table.setPrice(event.getEvent().getPrice());
                        table.setTime(event.getTime());
                        table.setDuration(event.getDuration().duration());
                        table.setRoom(event.getRoom());
                        table.setTherapist(event.getTherapist());
                        table.setClient(client);
                        tableItems.add(table);
                    });
                });

        concreteEventRepository.findByDateInRange(dateFrom, dateTo)
                .forEach((ConcreteEvent event) -> {
                    ReportTableItem table = new ReportTableItem();
                    table.setDate(event.getDate());
                    table.setServiceName(event.getService().getName());
                    table.setPrice(event.getService().getPrice());
                    table.setTime(event.getTime());
                    table.setDuration(event.getDuration().duration());
                    table.setRoom(event.getRoom());
                    table.setTherapist(event.getTherapist());
                    table.setClient(event.getClient());
                    tableItems.add(table);
                });

        return tableItems;
    }
}
