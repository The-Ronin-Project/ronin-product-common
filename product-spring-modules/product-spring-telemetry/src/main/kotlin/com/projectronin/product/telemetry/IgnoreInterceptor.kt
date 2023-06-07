package com.projectronin.product.telemetry

import datadog.trace.api.GlobalTracer
import datadog.trace.api.interceptor.MutableSpan
import datadog.trace.api.interceptor.TraceInterceptor

class IgnoreInterceptor(val startsWith: List<String>) : TraceInterceptor {
    override fun onTraceComplete(trace: MutableCollection<out MutableSpan>?): MutableCollection<out MutableSpan> {
        trace?.let {
            if (trace.any(::isIgnored)) {
                return mutableListOf()
            }
        }

        return trace!!
    }

    private fun isIgnored(mutableSpan: MutableSpan): Boolean {
        return startsWith.any { starter -> !mutableSpan.isError && mutableSpan.resourceName.startsWith(starter) }
    }

    override fun priority(): Int {
        return 200
    }

    companion object {
        fun create(startsWith: List<String>) {
            GlobalTracer.get().addTraceInterceptor(IgnoreInterceptor(startsWith))
        }
    }
}
