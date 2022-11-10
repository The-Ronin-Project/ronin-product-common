package com.projectronin.product.common.client.examples.eventpatient

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.lang.Nullable
import java.lang.StringBuilder
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

/**
 * EMR Patient V1
 *
 *
 * A patient resource from the EMR
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("create", "update", "delete")
@Generated("jsonschema2pojo")
class EmrPatientV1Schema {
    @Nullable
    @JsonProperty("create")
    var create: Patient? = null

    @Nullable
    @JsonProperty("update")
    var update: Patient? = null

    @Nullable
    @JsonProperty("delete")
    var delete: Delete? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param create
     * @param update
     * @param delete
     */
    constructor(create: Patient?, update: Patient?, delete: Delete?) : super() {
        this.create = create
        this.update = update
        this.delete = delete
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
        sb.append(EmrPatientV1Schema::class.java.name).append('@')
            .append(Integer.toHexString(System.identityHashCode(this))).append('[')
        sb.append("create")
        sb.append('=')
        sb.append(if (create == null) "<null>" else create)
        sb.append(',')
        sb.append("update")
        sb.append('=')
        sb.append(if (update == null) "<null>" else update)
        sb.append(',')
        sb.append("delete")
        sb.append('=')
        sb.append(if (delete == null) "<null>" else delete)
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
        result = result * 31 + if (create == null) 0 else create.hashCode()
        result = result * 31 + if (update == null) 0 else update.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (delete == null) 0 else delete.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is EmrPatientV1Schema == false) {
            return false
        }
        val rhs = other
        return (create === rhs.create || create != null && create == rhs.create) && (update === rhs.update || update != null && update == rhs.update) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (delete === rhs.delete || delete != null && delete == rhs.delete)
    }
}
