package com.projectronin.product.common.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan

@AutoConfiguration
@ComponentScan(basePackages = ["com.projectronin.product.common.exception.advice", "com.projectronin.product.common.exception.auth"])
@ConditionalOnProperty(prefix = "ronin.product.exceptions", name = ["advice"], matchIfMissing = true)
open class ExceptionConfig
