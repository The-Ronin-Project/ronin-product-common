package com.projectronin.product.audit

import com.projectronin.kafka.data.RoninWrapper
import com.projectronin.product.audit.config.AuditProperties
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import com.projectronin.product.common.auth.RoninAuthentication
import com.projectronin.product.common.auth.token.RoninClaims
import com.projectronin.product.common.auth.token.RoninUser
import com.projectronin.product.common.auth.token.RoninUserType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class AuditorTest {
    //    val producer = mockk<KafkaProducer<String, RoninWrapper<AuditCommandV1>>>(relaxed = true)
    val mockProducer = MockProducer(
        true,
        StringSerializer(),
        Serializer<RoninWrapper<AuditCommandV1>> { topic, data -> "_".toByteArray() }
    )
    val auditProperties = AuditProperties("audit.dlq.v1", "testSource")
    val auditor = Auditor(mockProducer, auditProperties)

    @BeforeEach
    fun setup() {
        mockkStatic(SecurityContextHolder::class)
        val holder = mockk<SecurityContext>()
        val authentication = stubRoninAuth()
        every { holder.authentication } returns (authentication)
        every { SecurityContextHolder.getContext() } returns (holder)
    }

    @Test
    fun `Audit a read action writes to kafka no data or mrn`() {
        auditor.read("cat", "type", "id")
        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninWrapper = producerRecord.value()
        assertThat(roninWrapper.tenantId).isEqualTo("apposnd")
        assertThat(roninWrapper.sourceService).isEqualTo("testSource")
        assertThat(roninWrapper.dataType).isEqualTo("AuditCommandV1")
        val auditCommand = roninWrapper.data
        assertThat(auditCommand.action).isEqualTo("READ")
        assertThat(auditCommand.mrn).isEqualTo("")
        assertThat(auditCommand.dataMap).isEqualTo(null)
        assertThat(auditCommand.tenantId).isEqualTo("apposnd")
        assertThat(auditCommand.userId).isEqualTo("user1234")
        assertThat(auditCommand.resourceCategory).isEqualTo("cat")
        assertThat(auditCommand.resourceType).isEqualTo("type")
        assertThat(auditCommand.resourceId).isEqualTo("id")
        assertThat(auditCommand.userFirstName).isEqualTo("first")
        assertThat(auditCommand.userLastName).isEqualTo("last")
        assertThat(auditCommand.userFullName).isEqualTo("first last")
    }

    @Test
    fun `Audit a read action writes to kafka with data and mrn`() {
        auditor.read("cat", "type", "id", mapOf("test" to "map"), "mrn")
        val producerRecord = mockProducer.history().get(0)

        val auditCommand = producerRecord.value().data
        assertThat(auditCommand.action).isEqualTo("READ")
        assertThat(auditCommand.mrn).isEqualTo("mrn")
        assertThat(auditCommand.dataMap?.get(0)).isEqualTo("test:map")
    }

    @Test
    fun `Audit a update action writes to kafka no data or mrn`() {
        auditor.update("cat", "type", "id")

        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninWrapper = producerRecord.value()
        assertThat(roninWrapper.tenantId).isEqualTo("apposnd")
        assertThat(roninWrapper.sourceService).isEqualTo("testSource")
        assertThat(roninWrapper.dataType).isEqualTo("AuditCommandV1")
        val auditCommand = roninWrapper.data
        assertThat(auditCommand.action).isEqualTo("UPDATE")
        assertThat(auditCommand.mrn).isEqualTo("")
        assertThat(auditCommand.dataMap).isEqualTo(null)
        assertThat(auditCommand.tenantId).isEqualTo("apposnd")
        assertThat(auditCommand.userId).isEqualTo("user1234")
        assertThat(auditCommand.resourceCategory).isEqualTo("cat")
        assertThat(auditCommand.resourceType).isEqualTo("type")
        assertThat(auditCommand.resourceId).isEqualTo("id")
        assertThat(auditCommand.userFirstName).isEqualTo("first")
        assertThat(auditCommand.userLastName).isEqualTo("last")
        assertThat(auditCommand.userFullName).isEqualTo("first last")
    }

    @Test
    fun `Audit a update action writes to kafka with data and mrn`() {
        auditor.update("cat", "type", "id", mapOf("test" to "map"), "mrn")
        val producerRecord = mockProducer.history().get(0)
        val auditCommand = producerRecord.value().data
        assertThat(auditCommand.action).isEqualTo("UPDATE")
        assertThat(auditCommand.mrn).isEqualTo("mrn")
        assertThat(auditCommand.dataMap?.get(0)).isEqualTo("test:map")
    }

    @Test
    fun `Audit a create action writes to kafka no data or mrn`() {
        auditor.create("cat", "type", "id")

        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninWrapper = producerRecord.value()
        assertThat(roninWrapper.tenantId).isEqualTo("apposnd")
        assertThat(roninWrapper.sourceService).isEqualTo("testSource")
        assertThat(roninWrapper.dataType).isEqualTo("AuditCommandV1")
        val auditCommand = roninWrapper.data
        assertThat(auditCommand.action).isEqualTo("CREATE")
        assertThat(auditCommand.mrn).isEqualTo("")
        assertThat(auditCommand.dataMap).isEqualTo(null)
        assertThat(auditCommand.tenantId).isEqualTo("apposnd")
        assertThat(auditCommand.userId).isEqualTo("user1234")
        assertThat(auditCommand.resourceCategory).isEqualTo("cat")
        assertThat(auditCommand.resourceType).isEqualTo("type")
        assertThat(auditCommand.resourceId).isEqualTo("id")
        assertThat(auditCommand.userFirstName).isEqualTo("first")
        assertThat(auditCommand.userLastName).isEqualTo("last")
        assertThat(auditCommand.userFullName).isEqualTo("first last")
    }

    @Test
    fun `Audit a create action writes to kafka with data and mrn`() {
        auditor.create("cat", "type", "id", mapOf("test" to "map"), "mrn")
        val producerRecord = mockProducer.history().get(0)
        val auditCommand = producerRecord.value().data
        assertThat(auditCommand.action).isEqualTo("CREATE")
        assertThat(auditCommand.mrn).isEqualTo("mrn")
        assertThat(auditCommand.dataMap?.get(0)).isEqualTo("test:map")
    }

    @Test
    fun `Audit a delete action writes to kafka no data or mrn`() {
        auditor.delete("cat", "type", "id")

        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninWrapper = producerRecord.value()
        assertThat(roninWrapper.tenantId).isEqualTo("apposnd")
        assertThat(roninWrapper.sourceService).isEqualTo("testSource")
        assertThat(roninWrapper.dataType).isEqualTo("AuditCommandV1")
        val auditCommand = roninWrapper.data
        assertThat(auditCommand.action).isEqualTo("DELETE")
        assertThat(auditCommand.mrn).isEqualTo("")
        assertThat(auditCommand.dataMap).isEqualTo(null)
        assertThat(auditCommand.tenantId).isEqualTo("apposnd")
        assertThat(auditCommand.userId).isEqualTo("user1234")
        assertThat(auditCommand.resourceCategory).isEqualTo("cat")
        assertThat(auditCommand.resourceType).isEqualTo("type")
        assertThat(auditCommand.resourceId).isEqualTo("id")
        assertThat(auditCommand.userFirstName).isEqualTo("first")
        assertThat(auditCommand.userLastName).isEqualTo("last")
        assertThat(auditCommand.userFullName).isEqualTo("first last")
    }

    @Test
    fun `Audit a delete action writes to kafka with data and mrn`() {
        auditor.delete("cat", "type", "id", mapOf("test" to "map"), "mrn")
        val producerRecord = mockProducer.history().get(0)
        val auditCommand = producerRecord.value().data
        assertThat(auditCommand.action).isEqualTo("DELETE")
        assertThat(auditCommand.mrn).isEqualTo("mrn")
        assertThat(auditCommand.dataMap?.get(0)).isEqualTo("test:map")
    }

    @Suppress("RedundantNullableReturnType")
    class stubRoninAuth : RoninAuthentication {
        override val tenantId: String
            get() = "apposnd"
        override val userId: String
            get() = "user1234"
        override val udpId: String?
            get() = "patient-1234"
        override val providerRoninId: String?
            get() = "provider_id"
        override val patientRoninId: String?
            get() = "patient_ronin_id"
        override val userFirstName: String
            get() = "first"
        override val userLastName: String
            get() = "last"
        override val userFullName: String
            get() = "first last"
        override val roninClaims: RoninClaims
            get() = RoninClaims(RoninUser("user1234", RoninUserType.Provider, null, null, listOf(), listOf()))

        override fun getName(): String {
            return "the name"
        }

        override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
            return mutableListOf()
        }

        override fun getCredentials(): Any {
            return "creds"
        }

        override fun getDetails(): Any {
            return "detes"
        }

        override fun getPrincipal(): Any {
            return "principal"
        }

        override fun isAuthenticated(): Boolean {
            return true
        }

        @Suppress("EmptyFunctionBlock")
        override fun setAuthenticated(isAuthenticated: Boolean) {
        }
    }
}
