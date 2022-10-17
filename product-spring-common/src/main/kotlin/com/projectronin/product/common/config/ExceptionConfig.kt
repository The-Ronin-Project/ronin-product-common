package com.projectronin.product.common.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("com.projectronin.product.common.exception.advice")
open class ExceptionConfig
