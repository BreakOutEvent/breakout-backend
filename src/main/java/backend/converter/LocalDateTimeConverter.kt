package backend.converter

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class LocalDateTimeConverter() : AttributeConverter<LocalDateTime, Timestamp> {

    override fun convertToDatabaseColumn(data: java.time.LocalDateTime?): java.sql.Timestamp? {
        return if (data != null) {
            java.sql.Timestamp.valueOf(data)
        } else {
            null
        }
    }

    override fun convertToEntityAttribute(data: java.sql.Timestamp?): java.time.LocalDateTime? {
        return data?.toLocalDateTime()
    }
}
