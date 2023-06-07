package com.projectronin.product.common.base

import mu.KLogger
import mu.KotlinLogging
import org.springframework.beans.factory.config.YamlProcessor
import org.springframework.beans.factory.config.YamlProcessor.DocumentMatcher
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import java.util.Properties

class ModulePropertySourceFactory : PropertySourceFactory {
    private val logger: KLogger = KotlinLogging.logger { }

    override fun createPropertySource(name: String?, resource: EncodedResource): PropertySource<*> {
        logger.info("Loading configuration from $name")
        val profile = System.getenv("SPRING_PROFILES_ACTIVE") ?: System.getProperty("spring.profiles.active")
        assert(profile != null)

        val yamlFactory = YamlPropertiesFactoryBean()
        yamlFactory.setDocumentMatchers(
            DocumentMatcher { properties: Properties ->
                val profileProperty = properties.getProperty("spring.profiles")
                if (profileProperty == null || profileProperty.isEmpty()) {
                    return@DocumentMatcher YamlProcessor.MatchStatus.ABSTAIN
                }
                if (profileProperty.contains(profile!!)) {
                    YamlProcessor.MatchStatus.FOUND
                } else {
                    YamlProcessor.MatchStatus.NOT_FOUND
                }
            }
        )
        yamlFactory.setResources(resource.resource)
        val properties = yamlFactory.getObject()
        return PropertiesPropertySource(resource.resource.filename!!, properties!!)
    }
}
