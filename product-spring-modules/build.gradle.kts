@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kover)
}

kover {
    engine.set(kotlinx.kover.api.DefaultIntellijEngine)
}
