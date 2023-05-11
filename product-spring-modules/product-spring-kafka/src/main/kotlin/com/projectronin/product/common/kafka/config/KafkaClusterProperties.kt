package com.projectronin.product.common.kafka.config

import com.projectronin.kafka.config.ClusterConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "ronin.kafka")
class KafkaClusterProperties @ConstructorBinding constructor(
    override val bootstrapServers: String,
    override val saslJaasConfig: String?,
    override val saslMechanism: String?,
    override val saslPassword: String?,
    override val saslUsername: String?,
    override val securityProtocol: String?
) : ClusterConfiguration
