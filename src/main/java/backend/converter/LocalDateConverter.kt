package backend.converter

import java.sql.Date
import java.time.LocalDate
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class LocalDateConverter() : AttributeConverter<LocalDate, Date> {
    override fun convertToDatabaseColumn(attribute: LocalDate?): Date? {
        if (attribute == null) return null
        return java.sql.Date.valueOf(attribute)
    }

    override fun convertToEntityAttribute(dbData: Date?): LocalDate? {
        if (dbData == null) return null
        return dbData.toLocalDate()
    }
}