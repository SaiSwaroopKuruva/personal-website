package finadvisor.service;

import finadvisor.dto.PageResponse;
import finadvisor.dto.security.DeviceResponse;
import finadvisor.dto.security.LoginHistoryEntryResponse;

import java.util.List;
import java.util.UUID;

public interface SecurityService {
    List<DeviceResponse> getDevices(String email);

    PageResponse<LoginHistoryEntryResponse> getLoginHistory(String email, int page, int size);

    void logoutAllDevices(String email);

    void revokeDevice(String email, UUID deviceId);
}
