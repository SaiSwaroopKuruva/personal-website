package finadvisor.mutualfund.dto;

import java.util.List;

public record FilterMetadataResponse(
        List<AmcOptionResponse> amcs,
        List<String> categories,
        List<String> subCategories,
        List<String> riskLevels,
        List<String> planTypes,
        List<String> optionTypes,
        List<String> assetClasses
) {
}
