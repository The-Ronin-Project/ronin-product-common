package com.projectronin.product.common.client.examples.eventpatient

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.lang.Nullable
import java.lang.StringBuilder
import java.net.URI
import java.util.ArrayList
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

/**
 * This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("emrText", "url", "coding")
@Generated("jsonschema2pojo")
class CodeableConcept {
    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("emrText")
    var emrText: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("url")
    var url: URI? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("coding")
    var coding: List<Coding>? = ArrayList()

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param coding
     * @param emrText
     * @param url
     */
    constructor(emrText: String?, url: URI?, coding: List<Coding>?) : super() {
        this.emrText = emrText
        this.url = url
        this.coding = coding
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
        sb.append(CodeableConcept::class.java.name).append('@')
            .append(Integer.toHexString(System.identityHashCode(this))).append('[')
        sb.append("emrText")
        sb.append('=')
        sb.append(if (emrText == null) "<null>" else emrText)
        sb.append(',')
        sb.append("url")
        sb.append('=')
        sb.append(if (url == null) "<null>" else url)
        sb.append(',')
        sb.append("coding")
        sb.append('=')
        sb.append(if (coding == null) "<null>" else coding)
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
        result = result * 31 + if (coding == null) 0 else coding.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (emrText == null) 0 else emrText.hashCode()
        result = result * 31 + if (url == null) 0 else url.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is CodeableConcept == false) {
            return false
        }
        val rhs = other
        return (coding === rhs.coding || coding != null && coding == rhs.coding) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (emrText === rhs.emrText || emrText != null && emrText == rhs.emrText) && (url === rhs.url || url != null && url == rhs.url)
    }
}
