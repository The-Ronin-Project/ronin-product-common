package com.projectronin.product.common.client.examples.eventpatient

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.lang.StringBuilder
import java.time.Instant
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("id", "tenantId", "deletedAt")
@Generated("jsonschema2pojo")
class Delete {
    /**
     *
     * (Required)
     *
     */
    @JsonProperty("id")
    var id: String? = null

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("tenantId")
    var tenantId: String? = null

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("deletedAt")
    var deletedAt: Instant? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param deletedAt
     * @param tenantId
     * @param id
     */
    constructor(id: String?, tenantId: String?, deletedAt: Instant?) : super() {
        this.id = id
        this.tenantId = tenantId
        this.deletedAt = deletedAt
    }

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any>? {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties!![name] = value
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(Delete::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("id")
        sb.append('=')
        sb.append(if (id == null) "<null>" else id)
        sb.append(',')
        sb.append("tenantId")
        sb.append('=')
        sb.append(if (tenantId == null) "<null>" else tenantId)
        sb.append(',')
        sb.append("deletedAt")
        sb.append('=')
        sb.append(if (deletedAt == null) "<null>" else deletedAt)
        sb.append(',')
        sb.append("additionalProperties")
        sb.append('=')
        sb.append(additionalProperties ?: "<null>")
        sb.append(',')
        if (sb[sb.length - 1] == ',') {
            sb.setCharAt(sb.length - 1, ']')
        } else {
            sb.append(']')
        }
        return sb.toString()
    }

    override fun hashCode(): Int {
        var result = 1
        result = result * 31 + if (tenantId == null) 0 else tenantId.hashCode()
        result = result * 31 + if (deletedAt == null) 0 else deletedAt.hashCode()
        result = result * 31 + if (id == null) 0 else id.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Delete == false) {
            return false
        }
        val rhs = other
        return (tenantId === rhs.tenantId || tenantId != null && tenantId == rhs.tenantId) && (deletedAt === rhs.deletedAt || deletedAt != null && deletedAt == rhs.deletedAt) && (id === rhs.id || id != null && id == rhs.id) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties)
    }
}
