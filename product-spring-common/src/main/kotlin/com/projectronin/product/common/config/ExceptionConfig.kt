package com.projectronin.product.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("com.projectronin.product.common.exception.advice")
@ConditionalOnProperty(prefix = "ronin.product.exceptions", name = ["advice"], matchIfMissing = true)
open class ExceptionConfig
