package com.projectronin.product.common.config

import com.projectronin.product.common.exception.response.api.ExceptionHandlingSettings
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
        @Value("\${ronin.product.exceptions.return.stacktraces:false}") returnStacktraces: Boolean
    ): ExceptionHandlingSettings {
        return ExceptionHandlingSettings(
            returnDetailMessages = returnDetailMessages,
            returnExceptionNames = returnExceptionNames,
            returnStacktraces = returnStacktraces
        )
    }
}
