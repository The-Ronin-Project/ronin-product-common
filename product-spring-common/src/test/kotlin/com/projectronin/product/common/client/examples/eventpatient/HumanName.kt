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
import java.util.ArrayList
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("use", "text", "family", "given", "prefix", "suffix", "period")
@Generated("jsonschema2pojo")
class HumanName {
    /**
     * The purpose of the name
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("use")
    @JsonPropertyDescription("The purpose of the name")
    var use: String? = null

    /**
     * Representation of the full name
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("text")
    @JsonPropertyDescription("Representation of the full name")
    var text: String? = null

    /**
     * Family name or Surname
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("family")
    @JsonPropertyDescription("Family name or Surname")
    var family: String? = null

    /**
     * Given names: first, middle
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("given")
    @JsonPropertyDescription("Given names: first, middle")
    var given: List<String>? = ArrayList()

    /**
     * Name parts before the name
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("prefix")
    @JsonPropertyDescription("Name parts before the name")
    var prefix: List<String>? = ArrayList()

    /**
     * Name parts after the name
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("suffix")
    @JsonPropertyDescription("Name parts after the name")
    var suffix: List<String>? = ArrayList()

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
     * @param given
     * @param period
     * @param use
     * @param prefix
     * @param text
     * @param family
     * @param suffix
     */
    constructor(
        use: String?,
        text: String?,
        family: String?,
        given: List<String>?,
        prefix: List<String>?,
        suffix: List<String>?,
        period: Period?
    ) : super() {
        this.use = use
        this.text = text
        this.family = family
        this.given = given
        this.prefix = prefix
        this.suffix = suffix
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
        sb.append(HumanName::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("use")
        sb.append('=')
        sb.append(if (use == null) "<null>" else use)
        sb.append(',')
        sb.append("text")
        sb.append('=')
        sb.append(if (text == null) "<null>" else text)
        sb.append(',')
        sb.append("family")
        sb.append('=')
        sb.append(if (family == null) "<null>" else family)
        sb.append(',')
        sb.append("given")
        sb.append('=')
        sb.append(if (given == null) "<null>" else given)
        sb.append(',')
        sb.append("prefix")
        sb.append('=')
        sb.append(if (prefix == null) "<null>" else prefix)
        sb.append(',')
        sb.append("suffix")
        sb.append('=')
        sb.append(if (suffix == null) "<null>" else suffix)
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
        result = result * 31 + if (given == null) 0 else given.hashCode()
        result = result * 31 + if (period == null) 0 else period.hashCode()
        result = result * 31 + if (use == null) 0 else use.hashCode()
        result = result * 31 + if (prefix == null) 0 else prefix.hashCode()
        result = result * 31 + if (text == null) 0 else text.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (family == null) 0 else family.hashCode()
        result = result * 31 + if (suffix == null) 0 else suffix.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is HumanName == false) {
            return false
        }
        val rhs = other
        return (given === rhs.given || given != null && given == rhs.given) && (period === rhs.period || period != null && period == rhs.period) && (use === rhs.use || use != null && use == rhs.use) && (prefix === rhs.prefix || prefix != null && prefix == rhs.prefix) && (text === rhs.text || text != null && text == rhs.text) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (family === rhs.family || family != null && family == rhs.family) && (suffix === rhs.suffix || suffix != null && suffix == rhs.suffix)
    }
}
