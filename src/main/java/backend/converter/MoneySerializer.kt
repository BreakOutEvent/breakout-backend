package backend.converter

import backend.model.misc.Url
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.javamoney.moneta.Money

class MoneySerializer : JsonSerializer<Money>() {
    override fun serialize(value: Money?, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value?.numberStripped)
    }
}

class UrlSerializer : JsonSerializer<Url>() {
    override fun serialize(value: Url?, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value?.toString())
    }
}

