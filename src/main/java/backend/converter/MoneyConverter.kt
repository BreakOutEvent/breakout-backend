package backend.converter

import org.javamoney.moneta.Money
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class MoneyConverter() : AttributeConverter<Money, String> {

    override fun convertToDatabaseColumn(attribute: Money): String {
        return attribute.toString()
    }

    override fun convertToEntityAttribute(dbData: String): Money {
        return Money.parse(dbData)
    }
}
