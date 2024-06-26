package com.projectronin.product.common.config

import com.projectronin.product.common.exception.response.api.DefaultErrorHandlingResponseEntityConstructor
import com.projectronin.product.common.exception.response.api.ErrorHandlingResponseEntityConstructor
import com.projectronin.product.common.exception.response.api.ExceptionHandlingSettings
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackages = ["com.projectronin.product.common.exception.advice", "com.projectronin.product.common.exception.auth"])
@ConditionalOnProperty(prefix = "ronin.product.exceptions", name = ["advice"], matchIfMissing = true)
open class ExceptionConfig {
    @Bean
    open fun exceptionHandlingSettings(
        @Value("\${ronin.product.exceptions.return.detail:false}") returnDetailMessages: Boolean,
        @Value("\${ronin.product.exceptions.return.exceptions:false}") returnExceptionNames: Boolean,
        @Value("\${ronin.product.exceptions.return.stacktraces:false}") returnStacktraces: Boolean,
        @Value("\${ronin.product.exceptions.log.level.http4xx:WARN}") logLevel4xx: Level,
        @Value("\${ronin.product.exceptions.log.level.http5xx:ERROR}") logLevel5xx: Level
    ): ExceptionHandlingSettings {
        return ExceptionHandlingSettings(
            returnDetailMessages = returnDetailMessages,
            returnExceptionNames = returnExceptionNames,
            returnStacktraces = returnStacktraces,
            logLevel4xx = logLevel4xx,
            logLevel5xx = logLevel5xx
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "ronin.product.exceptions", name = ["default"], matchIfMissing = true)
    open fun errorHandlingResponseEntityConstructor(): ErrorHandlingResponseEntityConstructor = DefaultErrorHandlingResponseEntityConstructor()
}
