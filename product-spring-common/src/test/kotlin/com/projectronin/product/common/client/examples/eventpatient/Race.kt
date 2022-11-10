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

/**
 * US Core race classification
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("ombCategory", "detailed", "emrText")
@Generated("jsonschema2pojo")
class Race {
    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("ombCategory")
    var ombCategory: List<Coding>? = ArrayList()

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("detailed")
    var detailed: List<Coding>? = ArrayList()

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("emrText")
    var emrText: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param emrText
     * @param detailed
     * @param ombCategory
     */
    constructor(ombCategory: List<Coding>?, detailed: List<Coding>?, emrText: String?) : super() {
        this.ombCategory = ombCategory
        this.detailed = detailed
        this.emrText = emrText
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
        sb.append(Race::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("ombCategory")
        sb.append('=')
        sb.append(if (ombCategory == null) "<null>" else ombCategory)
        sb.append(',')
        sb.append("detailed")
        sb.append('=')
        sb.append(if (detailed == null) "<null>" else detailed)
        sb.append(',')
        sb.append("emrText")
        sb.append('=')
        sb.append(if (emrText == null) "<null>" else emrText)
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
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (ombCategory == null) 0 else ombCategory.hashCode()
        result = result * 31 + if (emrText == null) 0 else emrText.hashCode()
        result = result * 31 + if (detailed == null) 0 else detailed.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Race == false) {
            return false
        }
        val rhs = other
        return (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (ombCategory === rhs.ombCategory || ombCategory != null && ombCategory == rhs.ombCategory) && (emrText === rhs.emrText || emrText != null && emrText == rhs.emrText) && (detailed === rhs.detailed || detailed != null && detailed == rhs.detailed)
    }
}
