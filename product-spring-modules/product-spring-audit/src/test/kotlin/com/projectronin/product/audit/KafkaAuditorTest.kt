package com.projectronin.product.audit

import com.projectronin.auth.RoninAuthentication
import com.projectronin.auth.token.RoninClaims
import com.projectronin.auth.token.RoninUser
import com.projectronin.auth.token.RoninUserType
import com.projectronin.kafka.data.RoninEvent
import com.projectronin.product.audit.config.AuditProperties
import com.projectronin.product.audit.messaging.v1.AuditCommandV1
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class KafkaAuditorTest {
    val mockProducer = MockProducer(
        true,
        StringSerializer(),
        Serializer<RoninEvent<AuditCommandV1>> { _, _ -> "_".toByteArray() }
    )
    val auditProperties = AuditProperties("audit.dlq.v1", "testSource")
    val auditor = KafkaAuditor(mockProducer, auditProperties)

    @BeforeEach
    fun setup() {
        mockkStatic(SecurityContextHolder::class)
        val holder = mockk<SecurityContext>()
        val authentication = FakeRoninAuth()
        every { holder.authentication } returns (authentication)
        every { SecurityContextHolder.getContext() } returns (holder)
    }

    @Test
    fun `test null producer`() {
        val localAuditor = KafkaAuditor(null, auditProperties)
        assertDoesNotThrow { localAuditor.read("cat", "type", "id") }
    }

    @Test
    fun `Audit a read action writes to kafka no data or mrn`() {
        auditor.read("cat", "type", "id")
        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninEvent = producerRecord.value()
        assertThat(roninEvent.tenantId?.value).isEqualTo("apposnd")
        assertThat(roninEvent.source).isEqualTo("testSource")
        assertThat(roninEvent.type).isEqualTo("AuditCommandV1")

        assertThat(roninEvent.data).isNotNull
        roninEvent.data.run {
            assertThat(action).isEqualTo("READ")
            assertThat(mrn).isEqualTo("")
            assertThat(dataMap).isEqualTo(null)
            assertThat(tenantId).isEqualTo("apposnd")
            assertThat(userId).isEqualTo("user1234")
            assertThat(resourceCategory).isEqualTo("cat")
            assertThat(resourceType).isEqualTo("type")
            assertThat(resourceId).isEqualTo("id")
            assertThat(userFirstName).isEqualTo("first")
            assertThat(userLastName).isEqualTo("last")
            assertThat(userFullName).isEqualTo("first last")
        }
    }

    @Test
    fun `Audit a read action writes to kafka with data and mrn`() {
        auditor.read("cat", "type", "id", mapOf("test" to "map"), "mrn")
        val producerRecord = mockProducer.history().get(0)

        val auditCommand = producerRecord.value().data
        assertThat(auditCommand).isNotNull
        auditCommand.run {
            assertThat(action).isEqualTo("READ")
            assertThat(mrn).isEqualTo("mrn")
            assertThat(dataMap?.get(0)).isEqualTo("test:map")
        }
    }

    @Test
    fun `Audit a update action writes to kafka no data or mrn`() {
        auditor.update("cat", "type", "id")

        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninWrapper = producerRecord.value()
        assertThat(roninWrapper.tenantId?.value).isEqualTo("apposnd")
        assertThat(roninWrapper.source).isEqualTo("testSource")
        assertThat(roninWrapper.type).isEqualTo("AuditCommandV1")
        val auditCommand = roninWrapper.data
        assertThat(auditCommand).isNotNull
        auditCommand.run {
            assertThat(action).isEqualTo("UPDATE")
            assertThat(mrn).isEqualTo("")
            assertThat(dataMap).isEqualTo(null)
            assertThat(tenantId).isEqualTo("apposnd")
            assertThat(userId).isEqualTo("user1234")
            assertThat(resourceCategory).isEqualTo("cat")
            assertThat(resourceType).isEqualTo("type")
            assertThat(resourceId).isEqualTo("id")
            assertThat(userFirstName).isEqualTo("first")
            assertThat(userLastName).isEqualTo("last")
            assertThat(userFullName).isEqualTo("first last")
        }
    }

    @Test
    fun `Audit a update action writes to kafka with data and mrn`() {
        auditor.update("cat", "type", "id", mapOf("test" to "map"), "mrn")
        val producerRecord = mockProducer.history().get(0)
        val auditCommand = producerRecord.value().data
        assertThat(auditCommand).isNotNull
        auditCommand.run {
            assertThat(action).isEqualTo("UPDATE")
            assertThat(mrn).isEqualTo("mrn")
            assertThat(dataMap?.get(0)).isEqualTo("test:map")
        }
    }

    @Test
    fun `Audit a create action writes to kafka no data or mrn`() {
        auditor.create("cat", "type", "id")

        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninEvent = producerRecord.value()
        assertThat(roninEvent.tenantId?.value).isEqualTo("apposnd")
        assertThat(roninEvent.source).isEqualTo("testSource")
        assertThat(roninEvent.type).isEqualTo("AuditCommandV1")
        val auditCommand = roninEvent.data
        assertThat(auditCommand).isNotNull
        auditCommand.run {
            assertThat(action).isEqualTo("CREATE")
            assertThat(mrn).isEqualTo("")
            assertThat(dataMap).isEqualTo(null)
            assertThat(tenantId).isEqualTo("apposnd")
            assertThat(userId).isEqualTo("user1234")
            assertThat(resourceCategory).isEqualTo("cat")
            assertThat(resourceType).isEqualTo("type")
            assertThat(resourceId).isEqualTo("id")
            assertThat(userFirstName).isEqualTo("first")
            assertThat(userLastName).isEqualTo("last")
            assertThat(userFullName).isEqualTo("first last")
        }
    }

    @Test
    fun `Audit a create action writes to kafka with data and mrn`() {
        auditor.create("cat", "type", "id", mapOf("test" to "map"), "mrn")
        val producerRecord = mockProducer.history().get(0)
        val auditCommand = producerRecord.value().data
        assertThat(auditCommand).isNotNull
        auditCommand.run {
            assertThat(action).isEqualTo("CREATE")
            assertThat(mrn).isEqualTo("mrn")
            assertThat(dataMap?.get(0)).isEqualTo("test:map")
        }
    }

    @Test
    fun `Audit a delete action writes to kafka no data or mrn`() {
        auditor.delete("cat", "type", "id")

        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninEvent = producerRecord.value()
        assertThat(roninEvent.tenantId?.value).isEqualTo("apposnd")
        assertThat(roninEvent.source).isEqualTo("testSource")
        assertThat(roninEvent.type).isEqualTo("AuditCommandV1")
        val auditCommand = roninEvent.data
        assertThat(auditCommand).isNotNull
        auditCommand.run {
            assertThat(action).isEqualTo("DELETE")
            assertThat(mrn).isEqualTo("")
            assertThat(dataMap).isEqualTo(null)
            assertThat(tenantId).isEqualTo("apposnd")
            assertThat(userId).isEqualTo("user1234")
            assertThat(resourceCategory).isEqualTo("cat")
            assertThat(resourceType).isEqualTo("type")
            assertThat(resourceId).isEqualTo("id")
            assertThat(userFirstName).isEqualTo("first")
            assertThat(userLastName).isEqualTo("last")
            assertThat(userFullName).isEqualTo("first last")
        }
    }

    @Test
    fun `Audit a delete action writes to kafka with data and mrn`() {
        auditor.delete("cat", "type", "id", mapOf("test" to "map"), "mrn")
        val producerRecord = mockProducer.history().get(0)
        val auditCommand = producerRecord.value().data
        assertThat(auditCommand).isNotNull
        auditCommand.run {
            assertThat(action).isEqualTo("DELETE")
            assertThat(mrn).isEqualTo("mrn")
            assertThat(dataMap?.get(0)).isEqualTo("test:map")
        }
    }

    @Test
    fun `Audit a generic action writes to kafka no data or mrn`() {
        auditor.writeAudit("FOO", "cat", "type", "id")
        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninEvent = producerRecord.value()
        assertThat(roninEvent.tenantId?.value).isEqualTo("apposnd")
        assertThat(roninEvent.source).isEqualTo("testSource")
        assertThat(roninEvent.type).isEqualTo("AuditCommandV1")

        assertThat(roninEvent.data).isNotNull
        roninEvent.data.run {
            assertThat(action).isEqualTo("FOO")
            assertThat(mrn).isEqualTo("")
            assertThat(dataMap).isEqualTo(null)
            assertThat(tenantId).isEqualTo("apposnd")
            assertThat(userId).isEqualTo("user1234")
            assertThat(resourceCategory).isEqualTo("cat")
            assertThat(resourceType).isEqualTo("type")
            assertThat(resourceId).isEqualTo("id")
            assertThat(userFirstName).isEqualTo("first")
            assertThat(userLastName).isEqualTo("last")
            assertThat(userFullName).isEqualTo("first last")
        }
    }

    @Test
    fun `Audit a generic action writes to kafka data and mrn`() {
        auditor.writeAudit(
            action = "FOO",
            resourceCategory = "cat",
            resourceType = "type",
            resourceId = "id",
            dataMap = mapOf(
                "string" to "value",
                "integer" to 3
            ),
            mrn = "mrn"
        )
        val producerRecord = mockProducer.history().get(0)
        assertThat(producerRecord.key()).isEqualTo("type:id")
        val roninEvent = producerRecord.value()
        assertThat(roninEvent.tenantId?.value).isEqualTo("apposnd")
        assertThat(roninEvent.source).isEqualTo("testSource")
        assertThat(roninEvent.type).isEqualTo("AuditCommandV1")

        assertThat(roninEvent.data).isNotNull
        roninEvent.data.run {
            assertThat(action).isEqualTo("FOO")
            assertThat(mrn).isEqualTo("mrn")
            assertThat(dataMap).containsExactlyInAnyOrder("string:value", "integer:3")
            assertThat(tenantId).isEqualTo("apposnd")
            assertThat(userId).isEqualTo("user1234")
            assertThat(resourceCategory).isEqualTo("cat")
            assertThat(resourceType).isEqualTo("type")
            assertThat(resourceId).isEqualTo("id")
            assertThat(userFirstName).isEqualTo("first")
            assertThat(userLastName).isEqualTo("last")
            assertThat(userFullName).isEqualTo("first last")
        }
    }

    @Suppress("RedundantNullableReturnType")
    class FakeRoninAuth : RoninAuthentication {
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
            get() = RoninClaims(RoninUser("user1234", RoninUserType.Provider, null, null, null, listOf(), listOf()))
        override val tokenValue: String
            get() = "token"

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
