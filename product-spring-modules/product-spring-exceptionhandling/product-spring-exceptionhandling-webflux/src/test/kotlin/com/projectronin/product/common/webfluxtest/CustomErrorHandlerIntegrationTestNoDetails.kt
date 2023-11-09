package com.projectronin.product.common.webfluxtest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(
    controllers = [TestEndpointController::class],
    useDefaultFilters = true,
    properties = [
        "ronin.product.exceptions.returnDetailMessages=false",
        "ronin.product.exceptions.returnExceptionNames=true",
        "ronin.product.exceptions.returnStacktraces=true"
    ]
)
class CustomErrorHandlerIntegrationTestNoDetails(@Autowired webTestClient: WebTestClient) : AbstractCustomErrorHandlerIntegrationTest(webTestClient) {
    override val expectDetails: Boolean = false
    override val expectExceptionNames: Boolean = true
    override val expectStacktraces: Boolean = true
}
