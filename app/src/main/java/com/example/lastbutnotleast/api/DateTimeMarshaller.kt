package com.example.lastbutnotleast.api

import com.google.gson.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Type

private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

class DateTimeMarshaller : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? = src?.run { JsonPrimitive(formatter.format(this)) }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime? = json?.asString?.run { LocalDateTime.parse(this, formatter) }
}
