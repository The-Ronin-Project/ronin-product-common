package com.projectronin.product.common.auth

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = ["com.projectronin.product.common.config", "com.projectronin.product.common.auth"])
@SpringBootConfiguration
open class SharedWebfluxConfigurationReference
