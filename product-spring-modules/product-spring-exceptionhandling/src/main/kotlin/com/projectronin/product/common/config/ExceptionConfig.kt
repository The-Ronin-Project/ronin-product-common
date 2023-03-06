package com.projectronin.product.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["com.projectronin.product.common.exception.advice", "com.projectronin.product.common.exception.auth"])
@ConditionalOnProperty(prefix = "ronin.product.exceptions", name = ["advice"], matchIfMissing = true)
open class ExceptionConfig
