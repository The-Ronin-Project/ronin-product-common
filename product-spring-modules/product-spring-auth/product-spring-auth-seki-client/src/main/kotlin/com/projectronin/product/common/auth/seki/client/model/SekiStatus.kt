package com.projectronin.product.common.auth.seki.client.model

import org.springframework.http.HttpStatus

class SekiStatus(val status: HttpStatus, val health: SekiHealth?, val rawResponse: String?, val exception: Throwable?)
