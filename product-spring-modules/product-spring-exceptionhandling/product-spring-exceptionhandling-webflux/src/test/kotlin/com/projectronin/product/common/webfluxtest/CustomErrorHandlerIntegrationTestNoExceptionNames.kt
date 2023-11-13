package com.projectronin.product.common.webfluxtest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(
    controllers = [TestEndpointController::class],
    useDefaultFilters = true,
    properties = [
        "ronin.product.exceptions.return.detail=true",
        "ronin.product.exceptions.return.exceptions=false",
        "ronin.product.exceptions.return.stacktraces=true"
    ]
)
class CustomErrorHandlerIntegrationTestNoExceptionNames(@Autowired webTestClient: WebTestClient) : AbstractCustomErrorHandlerIntegrationTest(webTestClient) {
    override val expectDetails: Boolean = true
    override val expectExceptionNames: Boolean = false
    override val expectStacktraces: Boolean = true
}
