package com.projectronin.product.common.webfluxtest

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.ComponentScan

/**
 * Class to load in config definitions that live outside of the current project
 *   (i.e. the 'common' project)
 */
@ComponentScan(basePackages = ["com.projectronin.product.common.config", "com.projectronin.product.common.webfluxtest"])
@SpringBootConfiguration
open class TestConfigurationReference
