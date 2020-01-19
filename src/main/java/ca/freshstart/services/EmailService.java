package ca.freshstart.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service("emailService")
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${email.host}")
    private String emailHost;
    @Value("${email.port}")
    private String emailPort;
    @Value("${email.from}")
    private String emailFrom;
    @Value("${email.username}")
    private String emailUser;
    @Value("${email.password}")
    private String emailPassword;

    public void sendEmail(String subject, String emailTo, String content) {
        Properties properties = System.getProperties();

        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.host", emailHost);
        properties.setProperty("mail.smtp.port", emailPort);
        properties.setProperty("mail.smtp.auth", "true");

        Session mailSession = Session.getInstance(properties, new SMTPAuthenticator());
        Transport transport = null;

        try {
            transport = mailSession.getTransport();

            String encodingOptions = "text/html; charset=utf-8";

            MimeMessage message = new MimeMessage(mailSession);
            message.setHeader("Content-Type", encodingOptions);
            message.setSubject(subject,"UTF-8");
            message.setContent(content, encodingOptions);
            message.setFrom(new InternetAddress(emailUser, emailFrom, "UTF-8"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));

            transport.connect();
            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch(Exception ex) {
            String msg = ex.getMessage();

            if(ex instanceof AddressException) {
                log.error("EmailService: wrong email = " + emailTo);
            }

            log.error(msg, ex);

            // TODO what to do if email wont sent
        }
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(emailUser, emailPassword);
        }
    }
}
