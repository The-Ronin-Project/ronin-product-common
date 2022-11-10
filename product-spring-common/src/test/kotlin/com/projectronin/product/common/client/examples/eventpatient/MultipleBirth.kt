package com.projectronin.product.common.client.examples.eventpatient

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.lang.Nullable
import java.lang.StringBuilder
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("isMultipleBirth", "birthOrder")
@Generated("jsonschema2pojo")
class MultipleBirth {
    /**
     * Whether the Patient was part of a multiple birth
     * (Required)
     *
     */
    @JsonProperty("isMultipleBirth")
    @JsonPropertyDescription("Whether the Patient was part of a multiple birth")
    var isMultipleBirth: Boolean? = null

    /**
     * The order in which they were born
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("birthOrder")
    @JsonPropertyDescription("The order in which they were born")
    var birthOrder: Int? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param birthOrder
     * @param isMultipleBirth
     */
    constructor(isMultipleBirth: Boolean?, birthOrder: Int?) : super() {
        this.isMultipleBirth = isMultipleBirth
        this.birthOrder = birthOrder
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
        sb.append(MultipleBirth::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("isMultipleBirth")
        sb.append('=')
        sb.append(if (isMultipleBirth == null) "<null>" else isMultipleBirth)
        sb.append(',')
        sb.append("birthOrder")
        sb.append('=')
        sb.append(if (birthOrder == null) "<null>" else birthOrder)
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
        result = result * 31 + if (birthOrder == null) 0 else birthOrder.hashCode()
        result = result * 31 + if (isMultipleBirth == null) 0 else isMultipleBirth.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is MultipleBirth == false) {
            return false
        }
        val rhs = other
        return (birthOrder === rhs.birthOrder || birthOrder != null && birthOrder == rhs.birthOrder) && (isMultipleBirth === rhs.isMultipleBirth || isMultipleBirth != null && isMultipleBirth == rhs.isMultipleBirth) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties)
    }
}
