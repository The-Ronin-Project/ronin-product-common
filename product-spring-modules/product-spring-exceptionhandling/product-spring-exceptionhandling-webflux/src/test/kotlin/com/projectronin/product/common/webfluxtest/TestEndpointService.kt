package com.projectronin.product.common.webfluxtest

import org.springframework.stereotype.Service
import java.util.UUID

interface TestEndpointService {

    fun getTestResponse(): TestResponse
}

@Service
open class TestEndpointServiceImpl : TestEndpointService {
    override fun getTestResponse(): TestResponse = TestResponse(UUID.randomUUID().toString())
}
