package com.projectronin.product

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

springBoot {
    buildInfo()
}
