package com.projectronin.product.plugin.openapi

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class OpenApiKotlinGeneratorTest {

    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()

        project.plugins.apply("com.projectronin.product.openapi")

        // Verify the result
        assertThat(project.tasks.findByName("generateOpenApiCode")).isNotNull()
    }

}
