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
 * This defines the required attributed for a FHIR code
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("system", "code", "display", "version")
@Generated("jsonschema2pojo")
class Coding {
    /**
     * The coding system that defines what the code represents
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("system")
    @JsonPropertyDescription("The coding system that defines what the code represents")
    var system: String? = null

    /**
     * The specific code determined by the system that specifies the value.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("code")
    @JsonPropertyDescription("The specific code determined by the system that specifies the value.")
    var code: String? = null

    /**
     * The human readable value for the code
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("display")
    @JsonPropertyDescription("The human readable value for the code")
    var display: String? = null

    /**
     * The specific version of the system where the code is defined
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("version")
    @JsonPropertyDescription("The specific version of the system where the code is defined")
    var version: String? = null

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
     * @param code
     * @param display
     * @param version
     */
    constructor(system: String?, code: String?, display: String?, version: String?) : super() {
        this.system = system
        this.code = code
        this.display = display
        this.version = version
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
        sb.append(Coding::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("system")
        sb.append('=')
        sb.append(if (system == null) "<null>" else system)
        sb.append(',')
        sb.append("code")
        sb.append('=')
        sb.append(if (this.code == null) "<null>" else this.code)
        sb.append(',')
        sb.append("display")
        sb.append('=')
        sb.append(if (display == null) "<null>" else display)
        sb.append(',')
        sb.append("version")
        sb.append('=')
        sb.append(if (version == null) "<null>" else version)
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
        result = result * 31 + if (this.code == null) 0 else this.code.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (version == null) 0 else version.hashCode()
        result = result * 31 + if (display == null) 0 else display.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Coding == false) {
            return false
        }
        val rhs = other
        return (system === rhs.system || system != null && system == rhs.system) && (this.code === rhs.code || this.code != null && this.code == rhs.code) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (version === rhs.version || version != null && version == rhs.version) && (display === rhs.display || display != null && display == rhs.display)
    }
}
