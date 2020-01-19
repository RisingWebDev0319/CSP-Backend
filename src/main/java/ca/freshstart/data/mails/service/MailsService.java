package ca.freshstart.data.mails.service;

import ca.freshstart.data.availability.entity.AvRequest;
import ca.freshstart.data.availability.entity.AvTherapistRequest;
import ca.freshstart.data.concreteEvent.entity.ConcreteEvent;
import ca.freshstart.data.concreteEvent.entity.ConcreteEventChange;
import ca.freshstart.data.concreteEvent.interfaces.BaseConcreteEvent;
import ca.freshstart.data.concreteEvent.interfaces.BaseCrossEvent;
import ca.freshstart.data.mails.entity.Expressions;
import ca.freshstart.data.mails.entity.Mails;
import ca.freshstart.data.mails.repository.MailsRepository;

import java.lang.Exception;

import ca.freshstart.data.mails.types.ExpressionGetter;
import ca.freshstart.data.suggestions.entity.PreliminaryEvent;
import ca.freshstart.helpers.TaskManager;
import ch.qos.logback.core.util.COWArrayList;
import org.hibernate.internal.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import ca.freshstart.services.EmailService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.Null;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.sql.Time;
import java.util.Date;

@Service
public class MailsService extends EmailService {

    @Autowired
    protected MailsRepository mailsRepository;
    @Autowired
    protected TaskManager taskManager;

    protected Mails basicMail = null;

    @Value("${server.frontend:}")
    protected String frontendPath;

    /**
     * Universal method send any text
     *
     * @param mailTo
     * @param text
     * @return
     */
    public Mails sendBasic(String mailTo, String text) {
        return send("basic", mailTo,
                new ExpressionGetter("CONTENT", text));
    }

    /**
     * Mail to recover forgotten password
     *
     * @param mailTo
     * @param pass
     * @return
     */
    public Mails sendRecoveryPassword(String mailTo, String pass) {
        return send("password_recovery", mailTo,
                new ExpressionGetter("PASSWORD", pass));
    }

    /**
     * POST availability/requests
     *
     * @param mailTo
     * @param therapistRequest
     * @param requestId
     * @return
     */
    public Mails sendAvailabilityCreated(String mailTo, AvTherapistRequest therapistRequest, Long requestId) {

        String link = "<a href=\"" + frontendPath + "/#/therapist/availability/requests/" + requestId + "\">View details</a>";
        AvRequest request = therapistRequest.getAvRequest();

        return send("available_create", mailTo,
                new ExpressionGetter("LINK", link),
                new ExpressionGetter("MESSAGE", therapistRequest.getMessage()),
                new ExpressionGetter("DATE_START", dateFormat(request.getStartDate())),
                new ExpressionGetter("DATE_END", dateFormat(request.getEndDate()))

        );
    }

    public Mails sendAvailabilityDeleted(String mailTo, AvTherapistRequest therapistRequest) {

        AvRequest request = therapistRequest.getAvRequest();

        return send("available_delete", mailTo,
                new ExpressionGetter("DATE_START", dateFormat(request.getStartDate())),
                new ExpressionGetter("DATE_END", dateFormat(request.getEndDate()))

        );
    }

    /**
     * Concrete event was changed
     *
     * @param concreteEventChange
     * @param event
     * @param mailTo
     * @return
     */
    public Mails sendEventChange(ConcreteEventChange concreteEventChange, ConcreteEvent event, String mailTo) {
        String confirmUrl = "<a href=\"" + frontendPath +
                "/#/bookingEventConfirmation/" + concreteEventChange.getEventCode()
                + "\">CONFIRM</a>";

        List<ExpressionGetter> exps = getExpFromConcreteEvent(event);
        exps.add(new ExpressionGetter("LINK_CONFIRM", confirmUrl));
        return send("event_changed", mailTo, exps.toArray(new ExpressionGetter[]{}));
    }

    /**
     * Send notice mail about removed mail
     *
     * @param mailTo
     * @return
     */
    public Mails sendEventRemoved(ConcreteEvent event, String mailTo) {
        return send("event_removed", mailTo, getExpFromConcreteEvent(event).toArray(new ExpressionGetter[]{}));
    }

    /**
     * Mail about event that was created
     *
     * @param concreteEventChange
     * @param event
     * @param mailTo
     * @return
     */
    public Mails sendEventCreated(ConcreteEventChange concreteEventChange, ConcreteEvent event, String mailTo) {

        String confirmUrl = "<a href=\"" + frontendPath +
                "/#/bookingEventConfirmation/" + concreteEventChange.getEventCode()
                + "\">CONFIRM</a>";

        List<ExpressionGetter> exps = getExpFromConcreteEvent(event);
        exps.add(new ExpressionGetter("LINK_CONFIRM", confirmUrl));
        exps.add(new ExpressionGetter("ROOM_NAME", event.getRoom().getName()));
        exps.add(new ExpressionGetter("SERVICE_NAME", event.getService().getName()));
        return send("event_changed", mailTo, exps.toArray(new ExpressionGetter[]{}));
    }

    public Mails sendMultipleEventsCreated(PreliminaryEvent[] events) {

        return renderMultipleEventBody(events);
    }

    /**
     * Get general expressions from Event
     *
     * @param event
     * @return
     */
    protected ArrayList<ExpressionGetter> getExpFromConcreteEvent(BaseConcreteEvent event) {
        ArrayList<ExpressionGetter> exps = new ArrayList<ExpressionGetter>();

        exps.add(new ExpressionGetter("SERVICE_TIME", timeFormat(event.getTime())));
        exps.add(new ExpressionGetter("SERVICE_DATE", dateFormat(event.getDate())));
        exps.add(new ExpressionGetter("SERVICE_DURATION_PREP", event.getDuration().getPrep().toString()));
        exps.add(new ExpressionGetter("SERVICE_DURATION_CLEAN", event.getDuration().getClean().toString()));
        exps.add(new ExpressionGetter("SERVICE_DURATION_BASE", event.getDuration().getProcessing().toString()));
        exps.add(new ExpressionGetter("SERVICE_DURATION_FULL", event.getDuration().duration().toString()));

        return exps;
    }

    /**
     * Send rendered mail using MailTemplates
     *
     * @param type
     * @param mailTo
     * @param params
     * @return
     */
    private Mails send(String type, String mailTo, ExpressionGetter... params) {

        Mails mail = null;
        try {
            mail = mailsRepository.findByType(type);
            if (mail.getBody() == null) mail.setBody("");

            if (basicMail == null)
                basicMail = mailsRepository.findByType("basic");

        } catch (DataIntegrityViolationException ex) {
            throw new DataIntegrityViolationException("Can not find such mail");
        }

        if (mail != null) {
            Set<Expressions> exps = mail.getExpressions();
            for (Expressions e : exps) {
                for (ExpressionGetter e2 : params)
                    if (e.getExpression().equals(e2.expression))
                        e.setValue(e2.value);
            }
            mail.setExpressions(exps);

            String rendered = this.renderBody(mail.getBody(), exps);

            Set<Expressions> basicExps = basicMail.getExpressions().stream().map((Expressions e) -> {
                if (e.getExpression().equals("CONTENT")) e.setValue(rendered);
                return e;
            }).collect(Collectors.toCollection(HashSet::new));

            if (type.equals("basic"))
                mail.setBody(rendered);
            else
                mail.setBody(this.renderBasic(rendered));
        }
        if (mail.getActive())
            this.sendEmail(mail.getSubject(), mailTo, mail.getBody());

        return mail;
    }

    /**
     * Render text put the passed expressions
     *
     * @param text
     * @param literals
     * @return
     */
    private String renderBody(String text, Set<Expressions> literals) {
        Pattern pattern = Pattern.compile("%[^%\\s]+%");
        Matcher matcher = pattern.matcher(text);
        matcher = matcher.reset();
        StringBuffer buffer = new StringBuffer();
        matcher.matches();
        while (matcher.find()) {

            String match = matcher.group();
            match = match.replaceAll("[%]", " ").trim();

            for (Expressions exp : literals) {
                if (match.equals(exp.getExpression())) {
                    matcher.appendReplacement(buffer, exp.getValue());
                    break;
                }
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Extract mail template and rendering based on events[]
     *
     * @param events
     * @return
     */
    private Mails renderMultipleEventBody(BaseConcreteEvent[] events) {

        Mails mail = mailsRepository.findByType("event_create_confirm");
        if (basicMail == null)
            basicMail = mailsRepository.findByType("basic");

        String mailTo = "";

        int service_count = events.length;

        if (mail.getBody() != null) {
            String text = mail.getBody();

            Pattern pattern = Pattern.compile("(?:%SERVICES_START%)([\\s\\S]*)(?:%SERVICES_END%)");
            Matcher matcher = pattern.matcher(text);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                //todo we have block text
                String match = matcher.group();
                match = match.replaceAll(pattern.toString(), "$1").trim();
                String resultText = "";
                int indexService = 0;
                for (BaseConcreteEvent event : events) {
                    List<ExpressionGetter> params = getExpFromConcreteEvent(event);
                    indexService++;
                    mailTo = event.getTherapist().getEmail();
                    params.add(new ExpressionGetter("ROOM_NAME", event.getRoom().getName()));
                    params.add(new ExpressionGetter("SERVICE_NAME", event.getService().getName()));
                    params.add(new ExpressionGetter("SERVICES_COUNT", String.valueOf(service_count)));
                    params.add(new ExpressionGetter("SERVICE_NUMBER", String.valueOf(indexService)));
                    Set<Expressions> exps = mail.getExpressions();
                    for (Expressions e : exps) {
                        for (ExpressionGetter e2 : params)
                            if (e.getExpression().equals(e2.expression))
                                e.setValue(e2.value);
                    }
                    mail.setExpressions(exps);

                    resultText += renderBody(match, exps);
                }
                matcher.appendReplacement(buffer, resultText);
            }

            matcher.appendTail(buffer);
            String bodyText = buffer.toString();
                   bodyText = renderBasic(
                           renderBody(bodyText, new HashSet<Expressions>(
                                   Arrays.asList(new Expressions("SERVICES_COUNT", String.valueOf(service_count))))
                           )
                   );

            sendEmail(mail.getSubject(), mailTo,bodyText);
        }

        return mail;
    }

    /**
     * Render body with alone CONTENT expression
     *
     * @param text
     * @return
     */
    private String renderBasic(String text) {
        if (this.basicMail != null) {
            String body = this.basicMail.getBody();
            text = body.replaceAll("%CONTENT%", text);
        }
        return text;
    }

    /**
     * DateFormat in mails
     *
     * @param inputDate
     * @return
     */
    private String dateFormat(Date inputDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        return dateFormat.format(inputDate);
    }

    /**
     * Time format in mails
     *
     * @param inputTime
     * @return
     */
    private String timeFormat(Time inputTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        return timeFormat.format(inputTime);
    }

    /**
     * Send mails should be asynchronously
     *
     * @param subject
     * @param emailTo
     * @param content
     */
    @Override
    public void sendEmail(String subject, String emailTo, String content) {
        taskManager.submitTask(() -> {
            super.sendEmail(subject, emailTo, content);
        });
    }
}
