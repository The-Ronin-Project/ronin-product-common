package com.projectronin.product.telemetry

import datadog.trace.api.interceptor.MutableSpan
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IgnoreInterceptorTest {
    val trace = mutableListOf(
        createSpan("one - span"),
        createSpan("two - span"),
        createSpan("red - span"),
        createSpan("blue - span")
    )

    @Test
    fun `onTraceComplete filters correctly on one item`() {
        val interceptor = IgnoreInterceptor(listOf("one", "two"))
        val returnTraces = interceptor.onTraceComplete(trace)
        assertThat(returnTraces).isEmpty()
    }

    @Test
    fun `onTraceComplete filters correctly on different`() {
        val interceptor = IgnoreInterceptor(listOf("two"))
        val returnTraces = interceptor.onTraceComplete(trace)
        assertThat(returnTraces).isEmpty()
    }

    @Test
    fun `onTraceComplete doesn't filter`() {
        val interceptor = IgnoreInterceptor(listOf("no match"))
        val returnTraces = interceptor.onTraceComplete(trace)
        assertThat(returnTraces).isEqualTo(trace)
    }

    @Test
    fun `test prority`() {
        val interceptor = IgnoreInterceptor(listOf("no match"))
        assertThat(interceptor.priority()).isEqualTo(200)
    }

    private fun createSpan(name: String): MutableSpan {
        val span1 = mockk<MutableSpan>(relaxed = true)
        every { span1.resourceName } returns name
        return span1
    }
}
