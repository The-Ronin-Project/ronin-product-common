package com.projectronin.product.common.webfluxtest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(
    controllers = [TestEndpointController::class],
    useDefaultFilters = true,
    properties = [
        "ronin.product.exceptions.returnDetailMessages=true",
        "ronin.product.exceptions.returnExceptionNames=true",
        "ronin.product.exceptions.returnStacktraces=false"
    ]
)
class CustomErrorHandlerIntegrationTestNoStacktraces(@Autowired webTestClient: WebTestClient) : AbstractCustomErrorHandlerIntegrationTest(webTestClient) {
    override val expectDetails: Boolean = true
    override val expectExceptionNames: Boolean = true
    override val expectStacktraces: Boolean = false
}
