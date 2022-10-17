package com.projectronin.product.common.exception

// "wrapping" indicates if certain exceptions should be nested inside a HttpMessageNotReadableException,
//   to mimic actual internal SpringBoot behavior
//     reference: AbstractJackson2HttpMessageConverter::readJavaType
private const val DEFAULT_WRAP = true
