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
            // TODO: Shouldn't we scan for modules here
            addModule(JavaTimeModule())
            addModule(kotlinModule())

            propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)

            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
            configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)

            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // Default behavior sets null values to JVM property defaults
            // Source https://projectronin.atlassian.net/browse/DASH-5230
            configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)

            // TODO: Really?  We want to trim every string in every service?  No way to optionally include or exclude it?
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
