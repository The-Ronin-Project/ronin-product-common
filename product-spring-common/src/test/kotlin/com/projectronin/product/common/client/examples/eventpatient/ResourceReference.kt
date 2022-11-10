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
@JsonPropertyOrder("resource", "type", "reference", "identifier", "display")
@Generated("jsonschema2pojo")
class ResourceReference {
    /**
     * When this exists, it represents a reference to an actual resource. This is derived from the data specified in `reference`.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("resource")
    @JsonPropertyDescription("When this exists, it represents a reference to an actual resource. This is derived from the data specified in `reference`.")
    var resource: Resource? = null

    /**
     * The FHIR type attribute of the resource
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("type")
    @JsonPropertyDescription("The FHIR type attribute of the resource")
    var type: String? = null

    /**
     * This should refer to a `resource.type/resource.id` but could refer to an external resource
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("reference")
    @JsonPropertyDescription("This should refer to a `resource.type/resource.id` but could refer to an external resource")
    var reference: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("identifier")
    var identifier: Identifier? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("display")
    var display: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param reference
     * @param identifier
     * @param resource
     * @param display
     * @param type
     */
    constructor(
        resource: Resource?,
        type: String?,
        reference: String?,
        identifier: Identifier?,
        display: String?
    ) : super() {
        this.resource = resource
        this.type = type
        this.reference = reference
        this.identifier = identifier
        this.display = display
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
        sb.append(ResourceReference::class.java.name).append('@')
            .append(Integer.toHexString(System.identityHashCode(this))).append('[')
        sb.append("resource")
        sb.append('=')
        sb.append(if (resource == null) "<null>" else resource)
        sb.append(',')
        sb.append("type")
        sb.append('=')
        sb.append(if (type == null) "<null>" else type)
        sb.append(',')
        sb.append("reference")
        sb.append('=')
        sb.append(if (reference == null) "<null>" else reference)
        sb.append(',')
        sb.append("identifier")
        sb.append('=')
        sb.append(if (identifier == null) "<null>" else identifier)
        sb.append(',')
        sb.append("display")
        sb.append('=')
        sb.append(if (display == null) "<null>" else display)
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
        result = result * 31 + if (reference == null) 0 else reference.hashCode()
        result = result * 31 + if (identifier == null) 0 else identifier.hashCode()
        result = result * 31 + if (resource == null) 0 else resource.hashCode()
        result = result * 31 + if (display == null) 0 else display.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (type == null) 0 else type.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is ResourceReference == false) {
            return false
        }
        val rhs = other
        return (reference === rhs.reference || reference != null && reference == rhs.reference) && (identifier === rhs.identifier || identifier != null && identifier == rhs.identifier) && (resource === rhs.resource || resource != null && resource == rhs.resource) && (display === rhs.display || display != null && display == rhs.display) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (type === rhs.type || type != null && type == rhs.type)
    }
}
