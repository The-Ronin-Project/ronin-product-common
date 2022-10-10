package com.projectronin.product.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Set to use our custom Json ObjectMapper when marshalling/unmarshalling from a controller.
 *     Side Note: the original inspiration was to "auto trim all whitespace for ALL calls"
 * @see <a href="https://projectronin.atlassian.net/browse/DASH-3145">Original Bug DASH-3145</a>
 * @see <a href="https://github.com/projectronin/ronin-audit/pull/40">Original Pull Request</a>
 */
@Configuration
open class HttpMessageConvertor : WebMvcConfigurer {
    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>?>) {
        converters.add(mappingJackson2HttpMessageConverter())
    }

    @Bean
    open fun mappingJackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
        return MappingJackson2HttpMessageConverter().also { it.objectMapper = JsonProvider.objectMapper }
    }
}
