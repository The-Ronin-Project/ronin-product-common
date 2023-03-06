package com.projectronin.product.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class JsonConfigurationTest {
    @Test
    fun `provides ObjectMapper bean`() {
        val ctx = AnnotationConfigApplicationContext(JsonConfiguration::class.java)
        assertSame(JsonProvider.objectMapper, ctx.getBean<ObjectMapper>())
    }
}
