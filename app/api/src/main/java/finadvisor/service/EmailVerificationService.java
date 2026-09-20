package finadvisor.service;

public interface EmailVerificationService {
    void sendVerification(String email);

    void verify(String token);

    void resend(String email);
}
