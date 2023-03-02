package com.projectronin.product.contracttest.services

interface ContractServicesProvider {

    fun provideServices(): List<ContractTestService>
}
