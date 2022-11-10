package com.projectronin.product.common.client.examples.eventpatient

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.lang.Nullable
import java.lang.StringBuilder
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

/**
 * Link to another patient record that refers to this Patient
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("other", "type")
@Generated("jsonschema2pojo")
class Link {
    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("other")
    var other: ResourceReference? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("type")
    var type: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param other
     * @param type
     */
    constructor(other: ResourceReference?, type: String?) : super() {
        this.other = other
        this.type = type
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
        sb.append(Link::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("other")
        sb.append('=')
        sb.append(if (other == null) "<null>" else other)
        sb.append(',')
        sb.append("type")
        sb.append('=')
        sb.append(if (type == null) "<null>" else type)
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
        result = result * 31 + if (other == null) 0 else other.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (type == null) 0 else type.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Link == false) {
            return false
        }
        val rhs = other
        return (this.other === rhs.other || this.other != null && this.other == rhs.other) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (type === rhs.type || type != null && type == rhs.type)
    }
}
