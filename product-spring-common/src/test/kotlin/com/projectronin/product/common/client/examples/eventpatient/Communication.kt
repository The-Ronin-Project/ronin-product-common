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
@JsonPropertyOrder("language", "isPreferred")
@Generated("jsonschema2pojo")
class Communication {
    /**
     * This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.
     * (Required)
     *
     */
    @JsonProperty("language")
    @JsonPropertyDescription("This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.")
    var language: CodeableConcept? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("isPreferred")
    var isPreferred: Boolean? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param language
     * @param isPreferred
     */
    constructor(language: CodeableConcept?, isPreferred: Boolean?) : super() {
        this.language = language
        this.isPreferred = isPreferred
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
        sb.append(Communication::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("language")
        sb.append('=')
        sb.append(if (language == null) "<null>" else language)
        sb.append(',')
        sb.append("isPreferred")
        sb.append('=')
        sb.append(if (isPreferred == null) "<null>" else isPreferred)
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
        result = result * 31 + if (language == null) 0 else language.hashCode()
        result = result * 31 + if (isPreferred == null) 0 else isPreferred.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Communication == false) {
            return false
        }
        val rhs = other
        return (language === rhs.language || language != null && language == rhs.language) && (isPreferred === rhs.isPreferred || isPreferred != null && isPreferred == rhs.isPreferred) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties)
    }
}
