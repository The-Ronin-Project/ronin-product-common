package com.projectronin.product.common.auth.seki.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class SekiHealth(val alive: Boolean)
