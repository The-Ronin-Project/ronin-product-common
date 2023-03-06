package com.projectronin.product.common.config.jacksonfluxcontrollertests

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@WebFluxTest(SimpleWebfluxController::class)
@Import(SharedWebfluxConfigurationReference::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleWebfluxControllerTest(
    @Autowired val webTestClient: WebTestClient
) {
    @BeforeEach
    fun reset() {
        SimpleWebfluxController.receivedBody = null
    }

    @Test
    fun testNull() {
        val samplePostBody = """{"a": "foo", "b": null}"""

        // NOTE: the 'Date' response header is (currently) not available here,
        //   but _IS_ available when the app is running.

        webTestClient
            .post()
            .uri("/api/test/sample-object")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(samplePostBody)
            .exchange()
            .expectStatus().isOk
            .expectBody().json(samplePostBody)

        when (val body = SimpleWebfluxController.receivedBody) {
            is SampleObject -> {
                assertThat(body.a).isEqualTo("foo")
                assertThat(body.b).isNull()
            }

            else -> fail("Body ${SimpleWebfluxController.receivedBody} doesn't match")
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
        webTestClient
            .post()
            .uri("/api/test/sample-object")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(samplePostBody)
            .exchange()
            .expectStatus().isOk
            .expectBody().json("""{"a": "foo", "b": "baz"}""")

        when (val body = SimpleWebfluxController.receivedBody) {
            is SampleObject -> {
                assertThat(body.a).isEqualTo("foo")
                assertThat(body.b).isEqualTo("baz")
            }

            else -> fail("Body ${SimpleWebfluxController.receivedBody} doesn't match")
        }
    }

    @Test
    fun testDates() {
        val datePostBody = """{"a":"2022-01-25T15:44:52.000012345Z","b":"2020-05-06T05:06:07.000000993-06:00","c":"2015-12-31T23:59:59.000009999","d":"2022-02-01"}"""

        // NOTE: the 'Date' response header is (currently) not available here,
        //   but _IS_ available when the app is running.
        webTestClient
            .post()
            .uri("/api/test/date-holder")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(datePostBody)
            .exchange()
            .expectStatus().isOk
            .expectBody().json(datePostBody)

        when (val body = SimpleWebfluxController.receivedBody) {
            is DateHolder -> {
                assertThat(body.a).isEqualTo(ZonedDateTime.of(2022, 1, 25, 15, 44, 52, 12345, ZoneOffset.UTC))
                assertThat(body.b).isEqualTo(ZonedDateTime.of(2020, 5, 6, 5, 6, 7, 993, ZoneOffset.of("-0600")))
                assertThat(body.c).isEqualTo(LocalDateTime.of(2015, 12, 31, 23, 59, 59, 9999))
                assertThat(body.d).isEqualTo(LocalDate.of(2022, 2, 1))
            }

            else -> fail("Body ${SimpleWebfluxController.receivedBody} doesn't match")
        }
    }
}
