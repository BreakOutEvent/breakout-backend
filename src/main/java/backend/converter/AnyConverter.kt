package backend.converter

import com.fasterxml.jackson.databind.ObjectMapper
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class AnyConverter() : AttributeConverter<Any, String> {

    val mapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Any): String {
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): Any {
        return mapper.readValue(dbData, Any::class.java)
    }
}
