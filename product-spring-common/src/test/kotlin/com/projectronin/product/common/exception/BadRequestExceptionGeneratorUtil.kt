package com.projectronin.product.common.exception

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.core.MethodParameter
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import kotlin.reflect.KClass

// "wrapping" indicates if certain exceptions should be nested inside a HttpMessageNotReadableException,
//   to mimic actual internal SpringBoot behavior
//     reference: AbstractJackson2HttpMessageConverter::readJavaType
private const val DEFAULT_WRAP = true

