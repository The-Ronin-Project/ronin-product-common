package com.projectronin.product.common.config.corsmvccontrollertests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(SimpleController::class)
@Import(SharedConfigurationReference::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleControllerTest(
    @Autowired val mockMvc: MockMvc
) {
    @Test
    fun testCorsSuccess() {
        val resp = mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/sample-object")
                .header(HttpHeaders.ORIGIN, "https://www.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "origin", "x-requested-with")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        assertThat(resp.response.getHeaderValue(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)).isEqualTo("https://www.example.com")
    }

    @Test
    fun testCorsFailure() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/api/test/sample-object")
                .header(HttpHeaders.ORIGIN, "https://projectronin.io")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "origin", "x-requested-with")
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
}
