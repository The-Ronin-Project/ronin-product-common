package com.projectronin.product.common.management.actuator

import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.management.ThreadDumpEndpoint
import org.springframework.stereotype.Component

/**
 * Creates an additional actuator that allows viewing
 *   the threaddump in a more human-readable 'text format'
 *   in addition to still having the default 'json' format.
 * Accessed via: http(s)://host:port/actuator/threaddumptext
 * @see <a href="https://docs.spring.io/spring-boot/docs/2.3.5.RELEASE/actuator-api/html/#threaddump-retrieving-text">Retrieving the Thread Dump as Text</a>
 */
@Component
@Endpoint(id = "threaddumptext")
class ThreadDumpTextEndpoint {

    @ReadOperation(produces = ["text/plain;charset=UTF-8"])
    fun textThreadDump(): String {
        return delegate.textThreadDump()
    }

    companion object {
        private val delegate = ThreadDumpEndpoint()
    }
}
