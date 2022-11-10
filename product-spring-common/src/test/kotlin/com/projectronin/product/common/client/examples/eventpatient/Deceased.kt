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
@JsonPropertyOrder("isDeceased", "deceasedDateTime")
@Generated("jsonschema2pojo")
class Deceased {
    /**
     * Whether the Patient is deceased
     * (Required)
     *
     */
    @JsonProperty("isDeceased")
    @JsonPropertyDescription("Whether the Patient is deceased")
    var isDeceased: Boolean? = null

    /**
     * If the Patient is deceased this is the date/time of death.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("deceasedDateTime")
    @JsonPropertyDescription("If the Patient is deceased this is the date/time of death.")
    var deceasedDateTime: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param isDeceased
     * @param deceasedDateTime
     */
    constructor(isDeceased: Boolean?, deceasedDateTime: String?) : super() {
        this.isDeceased = isDeceased
        this.deceasedDateTime = deceasedDateTime
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
        sb.append(Deceased::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("isDeceased")
        sb.append('=')
        sb.append(if (isDeceased == null) "<null>" else isDeceased)
        sb.append(',')
        sb.append("deceasedDateTime")
        sb.append('=')
        sb.append(if (deceasedDateTime == null) "<null>" else deceasedDateTime)
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
        result = result * 31 + if (isDeceased == null) 0 else isDeceased.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (deceasedDateTime == null) 0 else deceasedDateTime.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Deceased == false) {
            return false
        }
        val rhs = other
        return (isDeceased === rhs.isDeceased || isDeceased != null && isDeceased == rhs.isDeceased) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (deceasedDateTime === rhs.deceasedDateTime || deceasedDateTime != null && deceasedDateTime == rhs.deceasedDateTime)
    }
}
