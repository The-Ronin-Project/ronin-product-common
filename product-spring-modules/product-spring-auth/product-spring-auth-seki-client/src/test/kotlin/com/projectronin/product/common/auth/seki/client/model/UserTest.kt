package com.projectronin.product.common.auth.seki.client.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserTest {

    /**
     * Test special 'convenience functions' added to the User object
     *   to get the First/Last/Full name  (which were added so folks
     *   don't have to deal with the nested 'Name' class inside of User)
     */
    @Test
    fun `get name values`() {
        val testFirstName = "Jane"
        val testLastName = "Doe"
        val testFullName = "$testFirstName $testLastName"
        val user = User(
            id = "userId123",
            tenantId = "tenantId456",
            identities = listOf(Identity("Bar.Seki.AuthStrategies.MDAToken", "fake_009")),
            name = Name().apply { firstName = testFirstName; lastName = testLastName; fullName = testFullName }
        )
        assertEquals(testFirstName, user.firstName, "mismatch expcted user.firstName")
        assertEquals(testLastName, user.lastName, "mismatch expcted user.lastName")
        assertEquals(testFullName, user.fullName, "mismatch expcted user.fullName")
    }

    /**
     * Test special 'convenience functions' handle of null values
     *   Namely a seki user response could have EITHER [empty string] or [null] values for names.
     *   Don't want to force consumers of the User object to have to check for both conditions,
     *   thus have the name fucntion return "" if the underlying value is either "" or null
     */
    @Test
    fun `get null name values`() {
        val user = User(
            id = "userId123",
            tenantId = "tenantId456",
            identities = listOf(Identity("Bar.Seki.ZZZ.MDAToken", "fake_019")),
            name = Name().apply { firstName = null; lastName = null; fullName = null }
        )
        assertEquals("", user.firstName, "expected null firstName to return empty string")
        assertEquals("", user.lastName, "expected null lastName to return empty string")
        assertEquals("", user.fullName, "expected null fullName to return empty string")
    }
}
