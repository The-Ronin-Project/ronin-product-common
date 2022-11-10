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

/**
 * When this exists, it represents a reference to an actual resource. This is derived from the data specified in `reference`.
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("type", "id")
@Generated("jsonschema2pojo")
class Resource {
    /**
     * This is the ronin resource type
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("type")
    @JsonPropertyDescription("This is the ronin resource type")
    var type: String? = null

    /**
     * This is the ronin ID for the resource
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("id")
    @JsonPropertyDescription("This is the ronin ID for the resource")
    var id: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param id
     * @param type
     */
    constructor(type: String?, id: String?) : super() {
        this.type = type
        this.id = id
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
        sb.append(Resource::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("type")
        sb.append('=')
        sb.append(if (type == null) "<null>" else type)
        sb.append(',')
        sb.append("id")
        sb.append('=')
        sb.append(if (id == null) "<null>" else id)
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
        result = result * 31 + if (id == null) 0 else id.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (type == null) 0 else type.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Resource == false) {
            return false
        }
        val rhs = other
        return (id === rhs.id || id != null && id == rhs.id) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (type === rhs.type || type != null && type == rhs.type)
    }
}
