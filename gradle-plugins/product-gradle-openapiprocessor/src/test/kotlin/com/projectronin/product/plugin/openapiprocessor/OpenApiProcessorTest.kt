package com.projectronin.product.plugin.openapiprocessor

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class OpenApiProcessorTest {

    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()

        project.plugins.apply("com.projectronin.product.openapiprocessor")

        // Verify the result
        assertThat(project.tasks.findByName("processOpenApi")).isNotNull()
    }

}
