package com.projectronin.product.common.exception

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.projectronin.product.common.exception.response.api.ExceptionHandlingSettings
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.http.HttpStatus
import ch.qos.logback.classic.Level as LogbackLevel

class ExceptionHandlingSettingsTest {

    @Test
    fun `should get the right detail settings`() {
        ExceptionHandlingSettings(
            returnDetailMessages = false,
            returnExceptionNames = false,
            returnStacktraces = false,
            logLevel4xx = Level.WARN,
            logLevel5xx = Level.ERROR
        )
        assertThat(ExceptionHandlingSettings.returnDetailMessages).isFalse()
        assertThat(ExceptionHandlingSettings.returnExceptionNames).isFalse()
        assertThat(ExceptionHandlingSettings.returnStacktraces).isFalse()

        ExceptionHandlingSettings(
            returnDetailMessages = true,
            returnExceptionNames = false,
            returnStacktraces = false,
            logLevel4xx = Level.WARN,
            logLevel5xx = Level.ERROR
        )
        assertThat(ExceptionHandlingSettings.returnDetailMessages).isTrue()
        assertThat(ExceptionHandlingSettings.returnExceptionNames).isFalse()
        assertThat(ExceptionHandlingSettings.returnStacktraces).isFalse()

        ExceptionHandlingSettings(
            returnDetailMessages = false,
            returnExceptionNames = true,
            returnStacktraces = false,
            logLevel4xx = Level.WARN,
            logLevel5xx = Level.ERROR
        )
        assertThat(ExceptionHandlingSettings.returnDetailMessages).isFalse()
        assertThat(ExceptionHandlingSettings.returnExceptionNames).isTrue()
        assertThat(ExceptionHandlingSettings.returnStacktraces).isFalse()

        ExceptionHandlingSettings(
            returnDetailMessages = false,
            returnExceptionNames = false,
            returnStacktraces = true,
            logLevel4xx = Level.WARN,
            logLevel5xx = Level.ERROR
        )
        assertThat(ExceptionHandlingSettings.returnDetailMessages).isFalse()
        assertThat(ExceptionHandlingSettings.returnExceptionNames).isFalse()
        assertThat(ExceptionHandlingSettings.returnStacktraces).isTrue()

        val logger = LoggerFactory.getLogger("foo")

        run {
            val (_, logEntries) = withCapturedLogs("foo", LogbackLevel.DEBUG) {
                ExceptionHandlingSettings.logFunction(HttpStatus.INTERNAL_SERVER_ERROR, logger)("Bar")
            }
            assertThat(logEntries[0].level).isEqualTo(LogbackLevel.ERROR)
            assertThat(logEntries[0].message).isEqualTo("Bar")
        }
    }

    @Test
    fun `should get the right levels for 5xx logging`() {
        ExceptionHandlingSettings(
            returnDetailMessages = false,
            returnExceptionNames = false,
            returnStacktraces = false,
            logLevel4xx = Level.WARN,
            logLevel5xx = Level.ERROR
        )
        val logger = LoggerFactory.getLogger("foo")

        val (_, logEntries) = withCapturedLogs("foo", LogbackLevel.DEBUG) {
            ExceptionHandlingSettings.logFunction(HttpStatus.INTERNAL_SERVER_ERROR, logger)("Bar")
            ExceptionHandlingSettings.exceptionLogFunction(HttpStatus.INTERNAL_SERVER_ERROR, logger)("Baz", RuntimeException("E!"))
        }
        assertThat(logEntries[0].level).isEqualTo(LogbackLevel.ERROR)
        assertThat(logEntries[0].message).isEqualTo("Bar")
        assertThat(logEntries[1].level).isEqualTo(LogbackLevel.ERROR)
        assertThat(logEntries[1].message).isEqualTo("Baz")
        assertThat(logEntries[1].throwableProxy.className).isEqualTo("java.lang.RuntimeException")
        assertThat(logEntries[1].throwableProxy.message).isEqualTo("E!")
    }

    @Test
    fun `should get the right levels for 4xx logging`() {
        ExceptionHandlingSettings(
            returnDetailMessages = false,
            returnExceptionNames = false,
            returnStacktraces = false,
            logLevel4xx = Level.WARN,
            logLevel5xx = Level.ERROR
        )
        val logger = LoggerFactory.getLogger("foo")

        val (_, logEntries) = withCapturedLogs("foo", LogbackLevel.DEBUG) {
            ExceptionHandlingSettings.logFunction(HttpStatus.BAD_REQUEST, logger)("Bar")
            ExceptionHandlingSettings.exceptionLogFunction(HttpStatus.BAD_REQUEST, logger)("Baz", RuntimeException("E!"))
        }
        assertThat(logEntries[0].level).isEqualTo(LogbackLevel.WARN)
        assertThat(logEntries[0].message).isEqualTo("Bar")
        assertThat(logEntries[1].level).isEqualTo(LogbackLevel.WARN)
        assertThat(logEntries[1].message).isEqualTo("Baz")
        assertThat(logEntries[1].throwableProxy.className).isEqualTo("java.lang.RuntimeException")
        assertThat(logEntries[1].throwableProxy.message).isEqualTo("E!")
    }

    @Test
    fun `should get the right levels for non-defaults`() {
        ExceptionHandlingSettings(
            returnDetailMessages = false,
            returnExceptionNames = false,
            returnStacktraces = false,
            logLevel4xx = Level.DEBUG,
            logLevel5xx = Level.INFO
        )
        val logger = LoggerFactory.getLogger("foo")

        val (_, logEntries) = withCapturedLogs("foo", LogbackLevel.DEBUG) {
            ExceptionHandlingSettings.logFunction(HttpStatus.BAD_REQUEST, logger)("Bar")
            ExceptionHandlingSettings.exceptionLogFunction(HttpStatus.INTERNAL_SERVER_ERROR, logger)("Baz", RuntimeException("E!"))
        }
        assertThat(logEntries[0].level).isEqualTo(LogbackLevel.DEBUG)
        assertThat(logEntries[0].message).isEqualTo("Bar")
        assertThat(logEntries[1].level).isEqualTo(LogbackLevel.INFO)
        assertThat(logEntries[1].message).isEqualTo("Baz")
        assertThat(logEntries[1].throwableProxy.className).isEqualTo("java.lang.RuntimeException")
        assertThat(logEntries[1].throwableProxy.message).isEqualTo("E!")
    }

    @Test
    fun `should get the right levels for some other response`() {
        ExceptionHandlingSettings(
            returnDetailMessages = false,
            returnExceptionNames = false,
            returnStacktraces = false,
            logLevel4xx = Level.INFO,
            logLevel5xx = Level.INFO
        )
        val logger = LoggerFactory.getLogger("foo")

        val (_, logEntries) = withCapturedLogs("foo", LogbackLevel.DEBUG) {
            ExceptionHandlingSettings.logFunction(HttpStatus.FOUND, logger)("Bar")
        }
        assertThat(logEntries[0].level).isEqualTo(LogbackLevel.ERROR)
        assertThat(logEntries[0].message).isEqualTo("Bar")
    }

    private fun <T> withCapturedLogs(loggerName: String, level: LogbackLevel, fn: () -> T): Pair<T, List<ILoggingEvent>> {
        val logWatcher = ListAppender<ILoggingEvent>()
        logWatcher.start()

        val logger = LoggerFactory.getLogger(loggerName) as Logger
        val originalLevel = logger.level
        logger.level = level

        val result = try {
            logger.addAppender(logWatcher)
            fn()
        } finally {
            logger.detachAppender(logWatcher)
            logWatcher.stop()
            logger.level = originalLevel
        }
        return Pair(result, logWatcher.list)
    }
}
