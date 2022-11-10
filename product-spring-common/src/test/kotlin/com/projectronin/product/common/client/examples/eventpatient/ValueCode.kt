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
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("url", "valueCode")
@Generated("jsonschema2pojo")
class ValueCode {
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
     * (Required)
     *
     */
    @JsonProperty("valueCode")
    var valueCode: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param valueCode
     * @param url
     */
    constructor(url: URI?, valueCode: String?) : super() {
        this.url = url
        this.valueCode = valueCode
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
        sb.append(ValueCode::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("url")
        sb.append('=')
        sb.append(if (url == null) "<null>" else url)
        sb.append(',')
        sb.append("valueCode")
        sb.append('=')
        sb.append(if (valueCode == null) "<null>" else valueCode)
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
        result = result * 31 + if (valueCode == null) 0 else valueCode.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (url == null) 0 else url.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is ValueCode == false) {
            return false
        }
        val rhs = other
        return (valueCode === rhs.valueCode || valueCode != null && valueCode == rhs.valueCode) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (url === rhs.url || url != null && url == rhs.url)
    }
}
