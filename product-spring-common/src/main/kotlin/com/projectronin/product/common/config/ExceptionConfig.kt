package com.projectronin.product.common.config

import com.projectronin.product.common.exception.advice.BindExceptionResponseGenerator
import com.projectronin.product.common.exception.advice.ConstraintViolationExceptionResponseGenerator
import com.projectronin.product.common.exception.advice.InternalErrorHandler
import com.projectronin.product.common.exception.advice.JsonMappingExceptionResponseGenerator
import com.projectronin.product.common.exception.advice.JsonProcessingExceptionResponseGenerator
import com.projectronin.product.common.exception.advice.MethodArgumentTypeMismatchExceptionResponseGenerator
import com.projectronin.product.common.exception.advice.SpringErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("com.projectronin.product.common.exception.advice")
open class ExceptionConfig
