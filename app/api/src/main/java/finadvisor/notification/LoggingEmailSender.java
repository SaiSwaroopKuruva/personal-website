package finadvisor.notification;

import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs outbound emails instead of dispatching them through a real provider.
 * Replace with an SMTP/provider-backed implementation (e.g. SES, SendGrid) for production use.
 */
@Component
public class LoggingEmailSender implements EmailSender {

    private static final Logger log = Logger.getLogger(LoggingEmailSender.class.getName());

    @Override
    public void send(String to, String subject, String body) {
        log.log(Level.INFO, () -> "Email to " + to + " | subject=" + subject + " | body=" + body);
    }
}
