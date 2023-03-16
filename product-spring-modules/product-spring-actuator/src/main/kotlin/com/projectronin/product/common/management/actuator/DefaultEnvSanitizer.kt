package com.projectronin.product.common.management.actuator

import org.springframework.boot.actuate.endpoint.SanitizableData
import org.springframework.boot.actuate.endpoint.SanitizingFunction
import java.util.regex.Pattern

/**
 * Basic Sanitizer to scrub out common values that shouldn't be exposed (passwords, tokens, etc)
 *
 * This is essentially a COPY OF THE LOGIC from the SpringBoot 2.x 'Sanitizer.java' class,
 *  with the primary intention of keeping the same Sanitizer logic that existed before.
 *
 *  @see <a href="https://github.com/spring-projects/spring-boot/blob/e1a8b1a1015f942b7584c72c74d72b8142b1b521/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/endpoint/Sanitizer.java#L51-L53">Sanitizer.java</a>
 */
class DefaultEnvSanitizer : SanitizingFunction {

    companion object {
        // values originally copied from SpringBoot 2.x 'Sanitizer.java'
        private val SANITIZE_KEY_PATTERNS: List<Pattern> =
            listOf(
                ".*password$", ".*secret$", ".*key$", ".*token$", ".*credentials.*", ".*vcap_services$",
                "^vcap\\.services.*$", ".*sun.java.command$", "^spring[._]application[._]json$"
            ).map { Pattern.compile(it, Pattern.CASE_INSENSITIVE) }
    }

    override fun apply(data: SanitizableData?): SanitizableData? {
        val key = data?.key
        if (key != null) {
            for (pattern in SANITIZE_KEY_PATTERNS) {
                if (pattern.matcher(key).matches()) {
                    return SanitizableData(data.propertySource, data.key, SanitizableData.SANITIZED_VALUE)
                }
            }
        }
        return data
    }
}
