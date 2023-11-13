package com.projectronin.product.common.exception

import com.projectronin.product.common.test.TestEndpointController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(
    controllers = [TestEndpointController::class],
    useDefaultFilters = true,
    properties = [
        "ronin.product.exceptions.return.detail=false",
        "ronin.product.exceptions.return.exceptions=true",
        "ronin.product.exceptions.return.stacktraces=true"
    ]
)
class CustomErrorHandlerIntegrationTestNoDetails(@Autowired mockMvc: MockMvc) : AbstractCustomErrorHandlerIntegrationTest(mockMvc) {
    override val expectDetails: Boolean = false
    override val expectExceptionNames: Boolean = true
    override val expectStacktraces: Boolean = true
}
