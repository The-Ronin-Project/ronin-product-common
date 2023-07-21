package com.projectronin.product.common.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springdoc.core.configuration.SpringDocConfiguration
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.providers.ObjectMapperProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

@SpringBootTest(classes = [OpenApiFromContractConfiguration::class])
class OpenApiFromContractWebMVCConfigurationTest {

    @Autowired
    private lateinit var ctx: ApplicationContext

    @Test
    fun `assert seki auth is known by spring doc`() {
        assertThat(ctx.getBeansOfType(SpringDocConfiguration::class.java)).hasSize(1)
        assertThat(ctx.getBeansOfType(SpringDocConfigProperties::class.java)).hasSize(1)
        assertThat(ctx.getBeansOfType(ObjectMapperProvider::class.java)).hasSize(1)
    }
}
