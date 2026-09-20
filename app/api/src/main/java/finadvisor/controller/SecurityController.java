package finadvisor.controller;

import finadvisor.dto.PageResponse;
import finadvisor.dto.security.DeviceResponse;
import finadvisor.dto.security.LoginHistoryEntryResponse;
import finadvisor.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Tag(name = "Account Security", description = "Devices, login history and session revocation")
public class SecurityController {

    private final SecurityService securityService;

    @GetMapping("/devices")
    @Operation(summary = "List devices that have signed in to this account")
    public ResponseEntity<List<DeviceResponse>> getDevices(Authentication authentication) {
        return ResponseEntity.ok(securityService.getDevices(authentication.getName()));
    }

    @GetMapping("/login-history")
    @Operation(summary = "Get a paginated login/security event history")
    public ResponseEntity<PageResponse<LoginHistoryEntryResponse>> getLoginHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(securityService.getLoginHistory(authentication.getName(), page, size));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Log out of all devices and revoke every active session")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        securityService.logoutAllDevices(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/device/{id}")
    @Operation(summary = "Revoke a specific device and its sessions")
    public ResponseEntity<Void> revokeDevice(Authentication authentication, @PathVariable("id") UUID id) {
        securityService.revokeDevice(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
