package backend.converter

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class LocalDateTimeConverter() : AttributeConverter<LocalDateTime, Timestamp> {

    override fun convertToDatabaseColumn(data: java.time.LocalDateTime): java.sql.Timestamp {
        return java.sql.Timestamp.valueOf(data)
    }

    override fun convertToEntityAttribute(data: java.sql.Timestamp): java.time.LocalDateTime {
        return data.toLocalDateTime()
    }
}