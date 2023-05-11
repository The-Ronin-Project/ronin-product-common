package com.projectronin.product.common.kafka.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties

@AutoConfiguration
@ConditionalOnProperty(name = ["ronin.product.kafka"], matchIfMissing = true)
@EnableConfigurationProperties(KafkaClusterProperties::class)
open class KafkaConfiguration
