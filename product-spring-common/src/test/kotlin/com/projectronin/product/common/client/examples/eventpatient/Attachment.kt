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
@JsonPropertyOrder("contentType", "language", "url", "size", "hash", "title", "creation", "height", "width")
@Generated("jsonschema2pojo")
class Attachment {
    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("contentType")
    var contentType: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("language")
    var language: String? = null

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
    @JsonProperty("size")
    var size: Int? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("hash")
    var hash: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("title")
    var title: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("creation")
    var creation: String? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("height")
    var height: Int? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("width")
    var width: Int? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param size
     * @param width
     * @param language
     * @param title
     * @param contentType
     * @param url
     * @param hash
     * @param creation
     * @param height
     */
    constructor(
        contentType: String?,
        language: String?,
        url: URI?,
        size: Int?,
        hash: String?,
        title: String?,
        creation: String?,
        height: Int?,
        width: Int?
    ) : super() {
        this.contentType = contentType
        this.language = language
        this.url = url
        this.size = size
        this.hash = hash
        this.title = title
        this.creation = creation
        this.height = height
        this.width = width
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
        sb.append(Attachment::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("contentType")
        sb.append('=')
        sb.append(if (contentType == null) "<null>" else contentType)
        sb.append(',')
        sb.append("language")
        sb.append('=')
        sb.append(if (language == null) "<null>" else language)
        sb.append(',')
        sb.append("url")
        sb.append('=')
        sb.append(if (url == null) "<null>" else url)
        sb.append(',')
        sb.append("size")
        sb.append('=')
        sb.append(if (size == null) "<null>" else size)
        sb.append(',')
        sb.append("hash")
        sb.append('=')
        sb.append(if (hash == null) "<null>" else hash)
        sb.append(',')
        sb.append("title")
        sb.append('=')
        sb.append(if (title == null) "<null>" else title)
        sb.append(',')
        sb.append("creation")
        sb.append('=')
        sb.append(if (creation == null) "<null>" else creation)
        sb.append(',')
        sb.append("height")
        sb.append('=')
        sb.append(if (height == null) "<null>" else height)
        sb.append(',')
        sb.append("width")
        sb.append('=')
        sb.append(if (width == null) "<null>" else width)
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
        result = result * 31 + if (size == null) 0 else size.hashCode()
        result = result * 31 + if (width == null) 0 else width.hashCode()
        result = result * 31 + if (language == null) 0 else language.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (title == null) 0 else title.hashCode()
        result = result * 31 + if (contentType == null) 0 else contentType.hashCode()
        result = result * 31 + if (url == null) 0 else url.hashCode()
        result = result * 31 + if (hash == null) 0 else hash.hashCode()
        result = result * 31 + if (creation == null) 0 else creation.hashCode()
        result = result * 31 + if (height == null) 0 else height.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Attachment == false) {
            return false
        }
        val rhs = other
        return (size === rhs.size || size != null && size == rhs.size) && (width === rhs.width || width != null && width == rhs.width) && (language === rhs.language || language != null && language == rhs.language) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (title === rhs.title || title != null && title == rhs.title) && (contentType === rhs.contentType || contentType != null && contentType == rhs.contentType) && (url === rhs.url || url != null && url == rhs.url) && (hash === rhs.hash || hash != null && hash == rhs.hash) && (creation === rhs.creation || creation != null && creation == rhs.creation) && (height === rhs.height || height != null && height == rhs.height)
    }
}
