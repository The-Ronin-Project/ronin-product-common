package com.projectronin.product.common.client.examples.eventpatient

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.lang.Nullable
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("relationship", "name", "telecom", "address", "gender", "organization", "period")
@Generated("jsonschema2pojo")
class Contact {
    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("relationship")
    var relationship: List<CodeableConcept>? = ArrayList()

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("name")
    var name: HumanName? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("telecom")
    var telecom: List<ContactPoint>? = ArrayList()

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("address")
    var address: Address? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("gender")
    var gender: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("organization")
    var organization: ResourceReference? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("period")
    var period: Period? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param period
     * @param address
     * @param gender
     * @param organization
     * @param name
     * @param telecom
     * @param relationship
     */
    constructor(
        relationship: List<CodeableConcept>?,
        name: HumanName?,
        telecom: List<ContactPoint>?,
        address: Address?,
        gender: String?,
        organization: ResourceReference?,
        period: Period?
    ) : super() {
        this.relationship = relationship
        this.name = name
        this.telecom = telecom
        this.address = address
        this.gender = gender
        this.organization = organization
        this.period = period
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
        sb.append(Contact::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("relationship")
        sb.append('=')
        sb.append(if (relationship == null) "<null>" else relationship)
        sb.append(',')
        sb.append("name")
        sb.append('=')
        sb.append(if (name == null) "<null>" else name)
        sb.append(',')
        sb.append("telecom")
        sb.append('=')
        sb.append(if (telecom == null) "<null>" else telecom)
        sb.append(',')
        sb.append("address")
        sb.append('=')
        sb.append(if (address == null) "<null>" else address)
        sb.append(',')
        sb.append("gender")
        sb.append('=')
        sb.append(if (gender == null) "<null>" else gender)
        sb.append(',')
        sb.append("organization")
        sb.append('=')
        sb.append(if (organization == null) "<null>" else organization)
        sb.append(',')
        sb.append("period")
        sb.append('=')
        sb.append(if (period == null) "<null>" else period)
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
        result = result * 31 + if (period == null) 0 else period.hashCode()
        result = result * 31 + if (address == null) 0 else address.hashCode()
        result = result * 31 + if (gender == null) 0 else gender.hashCode()
        result = result * 31 + if (organization == null) 0 else organization.hashCode()
        result = result * 31 + if (name == null) 0 else name.hashCode()
        result = result * 31 + if (telecom == null) 0 else telecom.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (relationship == null) 0 else relationship.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Contact == false) {
            return false
        }
        val rhs = other
        return (period === rhs.period || period != null && period == rhs.period) && (address === rhs.address || address != null && address == rhs.address) && (gender === rhs.gender || gender != null && gender == rhs.gender) && (organization === rhs.organization || organization != null && organization == rhs.organization) && (name === rhs.name || name != null && name == rhs.name) && (telecom === rhs.telecom || telecom != null && telecom == rhs.telecom) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (relationship === rhs.relationship || relationship != null && relationship == rhs.relationship)
    }
}
