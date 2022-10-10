package com.projectronin.product.common.test

import org.springframework.stereotype.Service
import java.util.UUID

interface TestEndpointService {

    fun getTestResponse(): TestResponse
}

@Service
class TestEndpointServiceImpl : TestEndpointService {
    override fun getTestResponse(): TestResponse = TestResponse(UUID.randomUUID().toString())
}
