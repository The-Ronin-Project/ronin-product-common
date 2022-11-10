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
@JsonPropertyOrder("use", "type", "text", "line", "city", "district", "state", "postalCode", "country", "period")
@Generated("jsonschema2pojo")
class Address {
    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("use")
    var use: String? = null

    /**
     * distinguishes between postal and residence or both
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("type")
    @JsonPropertyDescription("distinguishes between postal and residence or both")
    var type: String? = null

    /**
     * display string of the address
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("text")
    @JsonPropertyDescription("display string of the address")
    var text: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("line")
    var line: List<String>? = ArrayList()

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("city")
    var city: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("district")
    var district: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("state")
    var state: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("postalCode")
    var postalCode: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("country")
    var country: String? = null

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
     * @param country
     * @param period
     * @param city
     * @param use
     * @param line
     * @param district
     * @param postalCode
     * @param text
     * @param state
     * @param type
     */
    constructor(
        use: String?,
        type: String?,
        text: String?,
        line: List<String>?,
        city: String?,
        district: String?,
        state: String?,
        postalCode: String?,
        country: String?,
        period: Period?
    ) : super() {
        this.use = use
        this.type = type
        this.text = text
        this.line = line
        this.city = city
        this.district = district
        this.state = state
        this.postalCode = postalCode
        this.country = country
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
        sb.append(Address::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("use")
        sb.append('=')
        sb.append(if (use == null) "<null>" else use)
        sb.append(',')
        sb.append("type")
        sb.append('=')
        sb.append(if (type == null) "<null>" else type)
        sb.append(',')
        sb.append("text")
        sb.append('=')
        sb.append(if (text == null) "<null>" else text)
        sb.append(',')
        sb.append("line")
        sb.append('=')
        sb.append(if (line == null) "<null>" else line)
        sb.append(',')
        sb.append("city")
        sb.append('=')
        sb.append(if (city == null) "<null>" else city)
        sb.append(',')
        sb.append("district")
        sb.append('=')
        sb.append(if (district == null) "<null>" else district)
        sb.append(',')
        sb.append("state")
        sb.append('=')
        sb.append(if (state == null) "<null>" else state)
        sb.append(',')
        sb.append("postalCode")
        sb.append('=')
        sb.append(if (postalCode == null) "<null>" else postalCode)
        sb.append(',')
        sb.append("country")
        sb.append('=')
        sb.append(if (country == null) "<null>" else country)
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
        result = result * 31 + if (country == null) 0 else country.hashCode()
        result = result * 31 + if (period == null) 0 else period.hashCode()
        result = result * 31 + if (city == null) 0 else city.hashCode()
        result = result * 31 + if (use == null) 0 else use.hashCode()
        result = result * 31 + if (line == null) 0 else line.hashCode()
        result = result * 31 + if (district == null) 0 else district.hashCode()
        result = result * 31 + if (postalCode == null) 0 else postalCode.hashCode()
        result = result * 31 + if (text == null) 0 else text.hashCode()
        result = result * 31 + if (state == null) 0 else state.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (type == null) 0 else type.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Address == false) {
            return false
        }
        val rhs = other
        return (country === rhs.country || country != null && country == rhs.country) && (period === rhs.period || period != null && period == rhs.period) && (city === rhs.city || city != null && city == rhs.city) && (use === rhs.use || use != null && use == rhs.use) && (line === rhs.line || line != null && line == rhs.line) && (district === rhs.district || district != null && district == rhs.district) && (postalCode === rhs.postalCode || postalCode != null && postalCode == rhs.postalCode) && (text === rhs.text || text != null && text == rhs.text) && (state === rhs.state || state != null && state == rhs.state) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (type === rhs.type || type != null && type == rhs.type)
    }
}
