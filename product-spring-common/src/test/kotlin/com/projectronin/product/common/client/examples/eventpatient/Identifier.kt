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
import java.net.URI
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("use", "type", "system", "value")
@Generated("jsonschema2pojo")
class Identifier {
    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("use")
    var use: String? = null

    /**
     * This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("type")
    @JsonPropertyDescription("This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.")
    var type: CodeableConcept? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("system")
    var system: URI? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("value")
    var value: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param system
     * @param use
     * @param type
     * @param value
     */
    constructor(use: String?, type: CodeableConcept?, system: URI?, value: String?) : super() {
        this.use = use
        this.type = type
        this.system = system
        this.value = value
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
        sb.append(Identifier::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("use")
        sb.append('=')
        sb.append(if (use == null) "<null>" else use)
        sb.append(',')
        sb.append("type")
        sb.append('=')
        sb.append(if (type == null) "<null>" else type)
        sb.append(',')
        sb.append("system")
        sb.append('=')
        sb.append(if (system == null) "<null>" else system)
        sb.append(',')
        sb.append("value")
        sb.append('=')
        sb.append(if (value == null) "<null>" else value)
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
        result = result * 31 + if (system == null) 0 else system.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (type == null) 0 else type.hashCode()
        result = result * 31 + if (value == null) 0 else value.hashCode()
        result = result * 31 + if (use == null) 0 else use.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Identifier == false) {
            return false
        }
        val rhs = other
        return (system === rhs.system || system != null && system == rhs.system) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (type === rhs.type || type != null && type == rhs.type) && (value === rhs.value || value != null && value == rhs.value) && (use === rhs.use || use != null && use == rhs.use)
    }
}
