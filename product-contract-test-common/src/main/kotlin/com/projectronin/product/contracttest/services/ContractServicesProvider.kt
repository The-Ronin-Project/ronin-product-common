@file:Suppress("DEPRECATION")

package com.projectronin.product.contracttest.services

import com.projectronin.domaintest.DomainTestServicesProvider
import com.projectronin.domaintest.DomainTestSetupContext
import com.projectronin.product.contracttest.LocalContractTestExtension

interface ContractServicesProvider : DomainTestServicesProvider {

    fun provideServices(): List<ContractTestService>

    override fun configurer(): DomainTestSetupContext.() -> Unit = {
        val allServices: LinkedHashSet<ContractTestService> = linkedSetOf()

        fun addWithDependencies(service: ContractTestService) {
            if (!allServices.contains(service)) {
                service.dependentServices.forEach(::addWithDependencies)
                println("Adding $service")
                allServices += service
            }
        }

        provideServices().forEach(::addWithDependencies)

        @Suppress("DEPRECATION")
        LocalContractTestExtension.addServices(allServices)

        allServices.forEach { it.setupAgainstDomainTest()() }
    }
}
