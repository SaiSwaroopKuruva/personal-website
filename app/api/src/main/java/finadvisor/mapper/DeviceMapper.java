package finadvisor.mapper;

import finadvisor.dto.security.DeviceResponse;
import finadvisor.entity.UserDevice;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

    public DeviceResponse toResponse(UserDevice device, boolean current) {
        return new DeviceResponse(
                device.getId(),
                device.getDeviceName(),
                device.getBrowser(),
                device.getIpAddress(),
                device.getLastLogin(),
                current);
    }
}
