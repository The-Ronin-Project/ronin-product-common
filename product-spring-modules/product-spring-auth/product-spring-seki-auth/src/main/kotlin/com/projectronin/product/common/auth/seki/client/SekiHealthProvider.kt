package com.projectronin.product.common.auth.seki.client

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

class SekiHealthProvider(
    val sekiClient: SekiClient
) : HealthIndicator {
    override fun health(): Health {
        val sekiHealth = sekiClient.checkHealth()
        return if (sekiHealth.exception != null) {
            Health.Builder().withException(sekiHealth.exception).down().build()
        } else {
            Health.Builder()
                .withDetail("httpStatus", sekiHealth.status)
                .run {
                    if (sekiHealth.status.is2xxSuccessful) {
                        if (sekiHealth.health?.alive == true) {
                            up()
                        } else {
                            down()
                        }
                    } else {
                        withDetail("httpResponse", sekiHealth.rawResponse ?: "").down()
                    }
                }
                .build()
        }
    }
}
