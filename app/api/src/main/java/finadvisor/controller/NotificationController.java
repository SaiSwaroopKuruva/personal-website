package finadvisor.controller;

import finadvisor.dto.notification.NotificationPreferencesResponse;
import finadvisor.dto.notification.UpdateNotificationPreferencesRequest;
import finadvisor.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Configure channel and category notification preferences")
public class NotificationController {

    private final NotificationPreferenceService notificationPreferenceService;

    @GetMapping("/preferences")
    @Operation(summary = "Get the current user's notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(Authentication authentication) {
        return ResponseEntity.ok(notificationPreferenceService.getPreferences(authentication.getName()));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Replace the current user's notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            Authentication authentication,
            @Valid @RequestBody UpdateNotificationPreferencesRequest request) {
        return ResponseEntity.ok(notificationPreferenceService.updatePreferences(authentication.getName(), request));
    }
}
