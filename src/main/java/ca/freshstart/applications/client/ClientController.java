package ca.freshstart.applications.client;

import ca.freshstart.data.session.entity.Session;
import ca.freshstart.data.session.repository.SessionRepository;
import ca.freshstart.helpers.ModelValidation;
import ca.freshstart.types.AbstractController;
import ca.freshstart.data.client.entity.Client;
import ca.freshstart.exceptions.BadRequestException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.Count;
import ca.freshstart.data.client.repository.ClientRepository;
import ca.freshstart.applications.client.helpers.CspSpecifications;
import ca.freshstart.helpers.CspUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
public class ClientController extends AbstractController {
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private SessionRepository sessionRepository;

    /**
     * Return list of clients
     *
     * @param pageId   page to show
     * @param pageSize Item on page to show (10 by default)
     * @param sort     Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested clients
     */
    @RequestMapping(value = "/clients", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Collection<Client> list(@RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                   @RequestParam(value = "sort", required = false) String sort) {

        PageRequest pageRequest = new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort));

        return clientRepository.findAllAsList(pageRequest);
    }

    /**
     * Search for clients with name containing several letters
     *
     * @param searchField  field to search for
     * @param searchString value to search. min length 3 chars
     * @param pageSize     Item on page to show (10 by default)
     * @param sort         Field to sort by, +/- prefix defines order (ACS/DESC)
     * @return Requested clients
     */
    @RequestMapping(value = "/clients/search", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Collection<Client> search(@RequestParam(value = "searchField", required = false, defaultValue = "name") String searchField,
                                     @RequestParam(value = "searchString", required = true) String searchString,
                                     @RequestParam(value = "pageId", required = false, defaultValue = "1") int pageId,
                                     @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                                     @RequestParam(value = "sort", required = false) String sort) {

        if (searchString != null && searchString.length() < 3) {
            throw new BadRequestException("Search value must be at least 3 characters long");
        }
        PageRequest pageRequest = new PageRequest(pageId - 1, pageSize, CspUtils.createSort(sort));
        Page<Client> page = clientRepository.findAll(CspSpecifications.findClients(sort, searchField, searchString), pageRequest);
        return page.getContent();
    }

    /**
     * Return count of clients
     *
     * @return Count of clients
     */
    @RequestMapping(value = "/clients/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count count() {

        return new Count(clientRepository.count());
    }

    @RequestMapping(value = "/client/{clientId}/sessions", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public List<Session> getSessionByClient(@PathVariable("clientId") Long clientId) {

            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new NotFoundException("No such client"));

        return sessionRepository.findByClients(client);
    }

    /**
     * Return information about one client
     *
     * @param clientId id of client
     * @return Client data
     */
    @RequestMapping(value = "/client/{clientId}", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Client getById(@PathVariable("clientId") Long clientId) {

        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("No such client"));
    }


    @RequestMapping(value = "/client/{clientId}", method = RequestMethod.PUT)
    @ResponseStatus(value = HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_SETTINGS")
    public void updateClient(@PathVariable("clientId") Long clientId, @RequestBody Client data) {
        try {
            clientRepository.findById(clientId)
                    .orElseThrow(() -> new NotFoundException("No such client"));

            clientRepository.save(data);
        }catch(DataIntegrityViolationException ex) {
            throw new NotFoundException("Failed update client: "+ex.getMessage());
        }
    }


    @RequestMapping(value = "/client/checkEmail", method = RequestMethod.POST)
    @Secured("ROLE_SETTINGS")
    public boolean isUniqueEmail(@RequestBody String email) {
        return !clientRepository.existsClientByEmail(email);
    }

    @RequestMapping(value = "/client", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK, reason = "Created")
    @Secured("ROLE_SETTINGS")
    public void createClient(@RequestBody Client data) {
        try {
            clientRepository.save(data);
        }catch(DataIntegrityViolationException ex) {
            throw new NotFoundException("Failed create client: "+ex.getMessage());
        }
    }

    @RequestMapping(value = "/client/{clientId}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK, reason = "Deleted")
    @Secured("ROLE_SETTINGS")
    public void deleteClient(@PathVariable("clientId") Long clientId) {
        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new NotFoundException("No such client"));

            client.setArchived(true);
            clientRepository.save(client);
        }catch(DataIntegrityViolationException ex) {
            throw new NotFoundException("Failed delete client: "+ex.getMessage());
        }
    }

}
