package finadvisor.controller;

import finadvisor.dto.PageResponse;
import finadvisor.dto.admin.AdminUserSummaryResponse;
import finadvisor.dto.admin.VerificationStatusResponse;
import finadvisor.dto.profile.ProfileResponse;
import finadvisor.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - User Management", description = "Administrative user management operations")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "List users (paginated)")
    public ResponseEntity<PageResponse<AdminUserSummaryResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminUserService.getUsers(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user's full profile")
    public ResponseEntity<ProfileResponse> getUser(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(adminUserService.getUser(id));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend a user account")
    public ResponseEntity<ProfileResponse> suspendUser(Authentication authentication, @PathVariable("id") UUID id) {
        return ResponseEntity.ok(adminUserService.suspendUser(id, authentication.getName()));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a suspended user account")
    public ResponseEntity<ProfileResponse> activateUser(Authentication authentication, @PathVariable("id") UUID id) {
        return ResponseEntity.ok(adminUserService.activateUser(id, authentication.getName()));
    }

    @PostMapping("/{id}/reset-risk-profile")
    @Operation(summary = "Reset a user's risk assessment history and profile")
    public ResponseEntity<Void> resetRiskProfile(Authentication authentication, @PathVariable("id") UUID id) {
        adminUserService.resetRiskProfile(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/verification-status")
    @Operation(summary = "Get a user's email/mobile/KYC verification status")
    public ResponseEntity<VerificationStatusResponse> getVerificationStatus(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(adminUserService.getVerificationStatus(id));
    }
}
