package finadvisor.service;

import finadvisor.dto.security.ChangePasswordRequest;
import finadvisor.dto.security.ForgotPasswordRequest;
import finadvisor.dto.security.ResetPasswordRequest;

public interface PasswordService {
    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(String email, ChangePasswordRequest request);
}
