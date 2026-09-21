package finadvisor.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReturnPeriodConverter implements AttributeConverter<ReturnPeriod, String> {

    @Override
    public String convertToDatabaseColumn(ReturnPeriod attribute) {
        return attribute == null ? null : attribute.code();
    }

    @Override
    public ReturnPeriod convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ReturnPeriod.fromCode(dbData);
    }
}
