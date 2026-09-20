package finadvisor.service.impl;

import finadvisor.dto.PageResponse;
import finadvisor.dto.security.DeviceResponse;
import finadvisor.dto.security.LoginHistoryEntryResponse;
import finadvisor.entity.User;
import finadvisor.entity.UserDevice;
import finadvisor.events.AuditEvent;
import finadvisor.exception.DeviceNotFoundException;
import finadvisor.exception.UserNotFoundException;
import finadvisor.mapper.DeviceMapper;
import finadvisor.repository.AuditLogRepository;
import finadvisor.repository.RefreshTokenRepository;
import finadvisor.repository.UserDeviceRepository;
import finadvisor.repository.UserRepository;
import finadvisor.security.RequestMetadata;
import finadvisor.security.RequestMetadataProvider;
import finadvisor.service.SecurityService;
import finadvisor.util.UserAgentParser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SecurityServiceImpl implements SecurityService {

    private static final Set<String> LOGIN_HISTORY_ACTIONS = Set.of("LOGIN", "REGISTER", "LOGOUT_ALL_DEVICES", "DEVICE_REVOKED");

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final DeviceMapper deviceMapper;
    private final RequestMetadataProvider requestMetadataProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getDevices(String email) {
        User user = findUser(email);
        RequestMetadata metadata = requestMetadataProvider.current();
        String currentBrowser = UserAgentParser.parseBrowser(metadata.userAgent());
        return userDeviceRepository.findByUser_IdOrderByLastLoginDesc(user.getId()).stream()
                .map(device -> deviceMapper.toResponse(device, isCurrentDevice(device, metadata, currentBrowser)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoginHistoryEntryResponse> getLoginHistory(String email, int page, int size) {
        User user = findUser(email);
        var result = auditLogRepository.findByUser_IdAndActionInOrderByCreatedAtDesc(
                user.getId(), LOGIN_HISTORY_ACTIONS, PageRequest.of(page, size));
        return PageResponse.of(result.map(log -> new LoginHistoryEntryResponse(
                log.getId(), log.getAction(), log.getIpAddress(), log.getCreatedAt())));
    }

    @Override
    public void logoutAllDevices(String email) {
        User user = findUser(email);
        refreshTokenRepository.deleteByUserId(user.getId());
        userDeviceRepository.deleteByUser_Id(user.getId());
        publishAudit(user, "LOGOUT_ALL_DEVICES", "All sessions and devices were revoked");
    }

    @Override
    public void revokeDevice(String email, UUID deviceId) {
        User user = findUser(email);
        UserDevice device = userDeviceRepository.findByIdAndUser_Id(deviceId, user.getId())
                .orElseThrow(() -> new DeviceNotFoundException("Device not found"));
        refreshTokenRepository.deleteByDevice_Id(device.getId());
        userDeviceRepository.delete(device);
        publishAudit(user, "DEVICE_REVOKED", "Device '" + device.getDeviceName() + "' was revoked");
    }

    private boolean isCurrentDevice(UserDevice device, RequestMetadata metadata, String currentBrowser) {
        return Objects.equals(device.getIpAddress(), metadata.ipAddress())
                && Objects.equals(device.getBrowser(), currentBrowser);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void publishAudit(User user, String action, String details) {
        eventPublisher.publishEvent(new AuditEvent(user.getId(), action, details,
                requestMetadataProvider.current().ipAddress()));
    }
}
