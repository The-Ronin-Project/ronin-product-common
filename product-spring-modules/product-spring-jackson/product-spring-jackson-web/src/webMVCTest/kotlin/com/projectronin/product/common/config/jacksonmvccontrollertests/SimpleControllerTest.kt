package com.projectronin.product.common.config.jacksonmvccontrollertests

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@WebMvcTest(SimpleController::class)
@Import(SharedConfigurationReference::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleControllerTest(
    @Autowired val mockMvc: MockMvc
) {
    @BeforeEach
    fun reset() {
        SimpleController.receivedBody = null
    }

    @Test
    fun testNull() {
        val samplePostBody = """{"a": "foo", "b": null}"""

        // NOTE: the 'Date' response header is (currently) not available here,
        //   but _IS_ available when the app is running.
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/test/sample-object")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(samplePostBody)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { res -> assertThat(res.response.contentAsString).matches("""\{"a": *"foo", *"b": *null}""") }
            .andReturn()

        when (val body = SimpleController.receivedBody) {
            is SampleObject -> {
                assertThat(body.a).isEqualTo("foo")
                assertThat(body.b).isNull()
            }

            else -> fail("Body ${SimpleController.receivedBody} doesn't match")
        }
    }

    /**
     * Shows that our object mapper is being used, since we're getting the trimming behavior we want.
     */
    @Test
    fun testTrimming() {
        val samplePostBody = """{"a": " foo  ", "b": " baz"}"""

        // NOTE: the 'Date' response header is (currently) not available here,
        //   but _IS_ available when the app is running.
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/test/sample-object")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(samplePostBody)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { res -> assertThat(res.response.contentAsString).matches("""\{"a": *"foo", *"b": *"baz"}""") }
            .andReturn()

        when (val body = SimpleController.receivedBody) {
            is SampleObject -> {
                assertThat(body.a).isEqualTo("foo")
                assertThat(body.b).isEqualTo("baz")
            }

            else -> fail("Body ${SimpleController.receivedBody} doesn't match")
        }
    }

    /**
     * Shows that our object mapper is being used, since we're getting the trimming behavior we want.
     */
    @Test
    fun testDates() {
        val datePostBody = """{"a":"2022-01-25T15:44:52.000012345Z","b":"2020-05-06T05:06:07.000000993-06:00","c":"2015-12-31T23:59:59.000009999","d":"2022-02-01"}"""

        // NOTE: the 'Date' response header is (currently) not available here,
        //   but _IS_ available when the app is running.
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/test/date-holder")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(datePostBody)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect { res -> assertThat(res.response.contentAsString).isEqualTo(datePostBody) }
            .andReturn()

        when (val body = SimpleController.receivedBody) {
            is DateHolder -> {
                assertThat(body.a).isEqualTo(ZonedDateTime.of(2022, 1, 25, 15, 44, 52, 12345, ZoneOffset.UTC))
                assertThat(body.b).isEqualTo(ZonedDateTime.of(2020, 5, 6, 5, 6, 7, 993, ZoneOffset.of("-0600")))
                assertThat(body.c).isEqualTo(LocalDateTime.of(2015, 12, 31, 23, 59, 59, 9999))
                assertThat(body.d).isEqualTo(LocalDate.of(2022, 2, 1))
            }

            else -> fail("Body ${SimpleController.receivedBody} doesn't match")
        }
    }
}
