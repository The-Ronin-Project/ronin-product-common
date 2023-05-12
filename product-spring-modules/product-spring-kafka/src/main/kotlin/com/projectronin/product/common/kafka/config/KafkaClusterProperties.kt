package com.projectronin.product.common.kafka.config

import com.projectronin.kafka.config.ClusterConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

@Suppress("LongParameterList")
@ConfigurationProperties(prefix = "ronin.kafka")
class KafkaClusterProperties @ConstructorBinding constructor(
    override val bootstrapServers: String,
    override val saslJaasConfig: String? = null,
    override val saslMechanism: String? = null,
    override val saslPassword: String? = null,
    override val saslUsername: String? = null,
    override val securityProtocol: String? = null,
    @DefaultValue("false")
    val disabled: Boolean = false
) : ClusterConfiguration
