package com.projectronin.product.common.exception.response.api

class ExceptionHandlingSettings(
    val returnDetailMessages: Boolean,
    val returnExceptionNames: Boolean,
    val returnStacktraces: Boolean
) {

    companion object {
        @Volatile
        private var settings: ExceptionHandlingSettings? = null
        val returnDetailMessages: Boolean
            get() = settings?.returnDetailMessages ?: false
        val returnExceptionNames: Boolean
            get() = settings?.returnExceptionNames ?: false
        val returnStacktraces: Boolean
            get() = settings?.returnStacktraces ?: false
    }

    init {
        ExceptionHandlingSettings.settings = this
    }
}
