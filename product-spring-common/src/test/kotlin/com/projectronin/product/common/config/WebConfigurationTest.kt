package com.projectronin.product.common.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebConfigurationTest {

    @Test
    fun `assert seki auth is known by spring doc`() {

        val bean = WebConfiguration().openAPI()
        assertEquals(1, bean.components.securitySchemes.size)
        assertEquals("seki", bean.components.securitySchemes.keys.first())
    }
}
