package com.projectronin.product.common.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import java.io.IOException

object JsonProvider {
    val objectMapper: ObjectMapper by lazy {
        jsonMapper {
            addModule(JavaTimeModule())
            addModule(kotlinModule())

            propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)

            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)

            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)

            val simpleModule = SimpleModule()
            simpleModule.addDeserializer(String::class.java, StringWithoutSpaceDeserializer(String::class.java))
            addModule(simpleModule)
        }
    }
}

class StringWithoutSpaceDeserializer(vc: Class<String>) : StdDeserializer<String>(vc) {
    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext) = p.text?.trim().orEmpty()
}
