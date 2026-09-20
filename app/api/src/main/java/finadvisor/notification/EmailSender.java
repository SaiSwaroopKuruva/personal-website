package finadvisor.notification;

/** Abstraction over outbound transactional email delivery. */
public interface EmailSender {
    void send(String to, String subject, String body);
}
