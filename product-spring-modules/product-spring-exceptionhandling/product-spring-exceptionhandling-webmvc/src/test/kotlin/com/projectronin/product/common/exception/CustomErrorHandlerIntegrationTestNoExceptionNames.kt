package com.projectronin.product.common.exception

import com.projectronin.product.common.test.TestEndpointController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(
    controllers = [TestEndpointController::class],
    useDefaultFilters = true,
    properties = [
        "ronin.product.exceptions.returnDetailMessages=true",
        "ronin.product.exceptions.returnExceptionNames=false",
        "ronin.product.exceptions.returnStacktraces=true"
    ]
)
class CustomErrorHandlerIntegrationTestNoExceptionNames(@Autowired mockMvc: MockMvc) : AbstractCustomErrorHandlerIntegrationTest(mockMvc) {
    override val expectDetails: Boolean = true
    override val expectExceptionNames: Boolean = false
    override val expectStacktraces: Boolean = true
}
