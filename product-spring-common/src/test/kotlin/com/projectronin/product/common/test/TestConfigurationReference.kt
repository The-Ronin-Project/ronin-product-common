package com.projectronin.product.common.test

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * Class to load in config definitions that live outside of the current project
 *   (i.e. the 'common' project)
 */
@ComponentScan("com.projectronin.product.common.config")
@ComponentScan("com.projectronin.product.common.exception")
@ComponentScan("com.projectronin.product.common.management")
@ComponentScan("com.projectronin.product.common.test")
@Configuration
open class TestConfigurationReference
