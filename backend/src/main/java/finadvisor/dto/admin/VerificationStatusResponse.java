package finadvisor.dto.admin;

public record VerificationStatusResponse(boolean emailVerified, boolean mobileVerified, String kycStatus) {
}
