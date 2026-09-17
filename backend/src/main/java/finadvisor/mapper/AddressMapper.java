package finadvisor.mapper;

import finadvisor.dto.profile.AddressResponse;
import finadvisor.entity.UserAddress;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressResponse toResponse(UserAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getType(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode(),
                address.isDefault(),
                address.getCreatedAt(),
                address.getUpdatedAt());
    }
}
