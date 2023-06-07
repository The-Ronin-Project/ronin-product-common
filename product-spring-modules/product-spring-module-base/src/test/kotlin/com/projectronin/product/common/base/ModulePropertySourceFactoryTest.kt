package com.projectronin.product.common.base

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.support.EncodedResource
import java.io.File

class ModulePropertySourceFactoryTest {

    @Test
    fun `Test factory reads yml null profile`() {
        System.clearProperty("spring.profiles.active")
        val factory = ModulePropertySourceFactory()
        val encodedResource = mockk<EncodedResource>(relaxed =  true)
        val resource = FileSystemResource(File("src/test/resources/test-application.yml"))

        every {  encodedResource.resource }.returns(resource)

        val source = factory.createPropertySource("sourceName", encodedResource)
        assertThat(source.getProperty("management.statsd.metrics.export.enabled")).isEqualTo(true)
        assertThat((source as PropertiesPropertySource).source.size).isEqualTo(3)
    }

    @Test
    fun `Test factory reads yml missing profile`() {
        System.setProperty("spring.profiles.active", "george")
        val factory = ModulePropertySourceFactory()
        val encodedResource = mockk<EncodedResource>(relaxed =  true)
        val resource = FileSystemResource(File("src/test/resources/test-application.yml"))

        every {  encodedResource.resource }.returns(resource)

        val source = factory.createPropertySource("sourceName", encodedResource)
        assertThat(source.getProperty("management.statsd.metrics.export.enabled")).isEqualTo(true)
        assertThat((source as PropertiesPropertySource).source.size).isEqualTo(3)
    }

    @Test
    fun `Test factory reads yml with profile local`() {
        System.setProperty("spring.profiles.active", "local")
        val factory = ModulePropertySourceFactory()
        val encodedResource = mockk<EncodedResource>(relaxed =  true)
        val resource = FileSystemResource(File("src/test/resources/test-application.yml"))

        every {  encodedResource.resource }.returns(resource)

        val source = factory.createPropertySource("sourceName", encodedResource)
        assertThat(source.getProperty("management.statsd.metrics.export.enabled")).isEqualTo(false)
        assertThat((source as PropertiesPropertySource).source.size).isEqualTo(16)
    }

    @Test
    fun `Test factory reads yml with profile test`() {
        System.setProperty("spring.profiles.active", "test")
        val factory = ModulePropertySourceFactory()
        val encodedResource = mockk<EncodedResource>(relaxed =  true)
        val resource = FileSystemResource(File("src/test/resources/test-application.yml"))

        every {  encodedResource.resource }.returns(resource)

        val source = factory.createPropertySource("sourceName", encodedResource)
        assertThat(source.getProperty("management.statsd.metrics.export.enabled")).isEqualTo(false)
        assertThat((source as PropertiesPropertySource).source.size).isEqualTo(5)
    }
}
