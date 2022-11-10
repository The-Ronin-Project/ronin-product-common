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
@JsonPropertyOrder("system", "originalSystem", "value", "use", "originalUse", "rank", "period")
@Generated("jsonschema2pojo")
class RoninContactPoint {
    /**
     * Normalized system
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("system")
    @JsonPropertyDescription("Normalized system")
    var system: String? = null

    /**
     * This defines the required attributed for a FHIR code
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("originalSystem")
    @JsonPropertyDescription("This defines the required attributed for a FHIR code")
    var originalSystem: Coding? = null

    /**
     * The actual phone number or email address
     * (Required)
     *
     */
    @JsonProperty("value")
    @JsonPropertyDescription("The actual phone number or email address")
    var value: String? = null

    /**
     * Normalized use
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("use")
    @JsonPropertyDescription("Normalized use")
    var use: String? = null

    /**
     * This defines the required attributed for a FHIR code
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("originalUse")
    @JsonPropertyDescription("This defines the required attributed for a FHIR code")
    var originalUse: Coding? = null

    /**
     * The preferred order of use
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("rank")
    @JsonPropertyDescription("The preferred order of use")
    var rank: Int? = null

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
     * @param period
     * @param system
     * @param originalUse
     * @param use
     * @param rank
     * @param value
     * @param originalSystem
     */
    constructor(
        system: String?,
        originalSystem: Coding?,
        value: String?,
        use: String?,
        originalUse: Coding?,
        rank: Int?,
        period: Period?
    ) : super() {
        this.system = system
        this.originalSystem = originalSystem
        this.value = value
        this.use = use
        this.originalUse = originalUse
        this.rank = rank
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
        sb.append(RoninContactPoint::class.java.name).append('@')
            .append(Integer.toHexString(System.identityHashCode(this))).append('[')
        sb.append("system")
        sb.append('=')
        sb.append(if (system == null) "<null>" else system)
        sb.append(',')
        sb.append("originalSystem")
        sb.append('=')
        sb.append(if (originalSystem == null) "<null>" else originalSystem)
        sb.append(',')
        sb.append("value")
        sb.append('=')
        sb.append(if (value == null) "<null>" else value)
        sb.append(',')
        sb.append("use")
        sb.append('=')
        sb.append(if (use == null) "<null>" else use)
        sb.append(',')
        sb.append("originalUse")
        sb.append('=')
        sb.append(if (originalUse == null) "<null>" else originalUse)
        sb.append(',')
        sb.append("rank")
        sb.append('=')
        sb.append(if (rank == null) "<null>" else rank)
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
        result = result * 31 + if (period == null) 0 else period.hashCode()
        result = result * 31 + if (system == null) 0 else system.hashCode()
        result = result * 31 + if (originalUse == null) 0 else originalUse.hashCode()
        result = result * 31 + if (use == null) 0 else use.hashCode()
        result = result * 31 + if (rank == null) 0 else rank.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (value == null) 0 else value.hashCode()
        result = result * 31 + if (originalSystem == null) 0 else originalSystem.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is RoninContactPoint == false) {
            return false
        }
        val rhs = other
        return (period === rhs.period || period != null && period == rhs.period) && (system === rhs.system || system != null && system == rhs.system) && (originalUse === rhs.originalUse || originalUse != null && originalUse == rhs.originalUse) && (use === rhs.use || use != null && use == rhs.use) && (rank === rhs.rank || rank != null && rank == rhs.rank) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (value === rhs.value || value != null && value == rhs.value) && (originalSystem === rhs.originalSystem || originalSystem != null && originalSystem == rhs.originalSystem)
    }
}
