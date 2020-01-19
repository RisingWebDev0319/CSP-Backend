package ca.freshstart.applications.mails;

import ca.freshstart.data.mails.service.MailsService;
import ca.freshstart.data.mails.entity.Mails;
import ca.freshstart.data.mails.repository.MailsRepository;
import ca.freshstart.data.mails.service.MailsService;
import ca.freshstart.exceptions.ConflictException;
import ca.freshstart.exceptions.NotFoundException;
import ca.freshstart.types.AbstractController;
import ca.freshstart.types.Count;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MailsController extends AbstractController {
    private final MailsRepository mailsRepository;
    private final MailsService mailsService;

    @RequestMapping(value = "/mails", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Collection <Mails> getList() {
        List<Mails> list = new ArrayList<>();

        mailsRepository.findAll().forEach(list::add);

        return list;
    }

    @ResponseStatus(value= HttpStatus.OK)
    @RequestMapping(value = "/mail/send", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public void sendMail(){
        // mailService.sendMultipleEventsCreated("dustudiod@yandex.ru","Любой текст");
    }

    @RequestMapping(value = "/mails/count", method = RequestMethod.GET)
    @Secured("ROLE_SETTINGS")
    public Count mailsCount() {
        return new Count(mailsRepository.count());
    }

    @RequestMapping(value = "/mails/{mailId}", method = RequestMethod.PUT)
    @ResponseStatus(value= HttpStatus.OK, reason = "Updated")
    @Secured("ROLE_AUTH")
    public void updateMail(@PathVariable("mailId") Long mailId, @RequestBody Mails mailBody) {
        Mails mail = mailsRepository.findById(mailId)
                .orElseThrow(() -> new NotFoundException("No such mail"));
        mail.setBody(mailBody.getBody());
        mail.setSubject(mailBody.getSubject());
        mail.setActive(mailBody.getActive());
        try {
            mailsRepository.save(mail);
        } catch(DataIntegrityViolationException ex) {
            throw new ConflictException("Saving email are happens with error");
        }
    }
}