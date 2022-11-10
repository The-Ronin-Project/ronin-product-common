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
import java.time.Instant
import java.time.LocalDate
import java.util.ArrayList
import java.util.LinkedHashMap
import javax.annotation.processing.Generated

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    "id",
    "fhirId",
    "tenantId",
    "mrn",
    "resourceVersion",
    "identifiers",
    "race",
    "ethnicity",
    "birthSex",
    "genderIdentity",
    "religion",
    "isActive",
    "name",
    "telecom",
    "gender",
    "birthDate",
    "deceased",
    "address",
    "maritalStatus",
    "multipleBirth",
    "photo",
    "contact",
    "communication",
    "generalPractitioner",
    "managingOrganization",
    "link",
    "createdAt",
    "updatedAt"
)
@Generated("jsonschema2pojo")
class Patient {
    /**
     * Ronnin Common ID for the Patient
     * (Required)
     *
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Ronnin Common ID for the Patient")
    var id: String? = null

    /**
     * The Patient's FHIR ID
     * (Required)
     *
     */
    @JsonProperty("fhirId")
    @JsonPropertyDescription("The Patient's FHIR ID")
    var fhirId: String? = null

    /**
     * Ronin tenant/organization the Patient is associated with
     * (Required)
     *
     */
    @JsonProperty("tenantId")
    @JsonPropertyDescription("Ronin tenant/organization the Patient is associated with")
    var tenantId: String? = null

    /**
     * EMR identifier for the Patient
     * (Required)
     *
     */
    @JsonProperty("mrn")
    @JsonPropertyDescription("EMR identifier for the Patient")
    var mrn: String? = null

    /**
     * An increasing version value
     * (Required)
     *
     */
    @JsonProperty("resourceVersion")
    @JsonPropertyDescription("An increasing version value")
    var resourceVersion: String? = null

    /**
     * Miscellaneous identifiers in the EMR. MRN, tenantId and fhirId in the FHIR representation are pulled up to core attributes and not listed here.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("identifiers")
    @JsonPropertyDescription("Miscellaneous identifiers in the EMR. MRN, tenantId and fhirId in the FHIR representation are pulled up to core attributes and not listed here.")
    var identifiers: List<Identifier>? = ArrayList()

    /**
     * This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("race")
    @JsonPropertyDescription("This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.")
    var race: CodeableConcept? = null

    /**
     * This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("ethnicity")
    @JsonPropertyDescription("This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.")
    var ethnicity: CodeableConcept? = null

    @Nullable
    @JsonProperty("birthSex")
    var birthSex: ValueCode? = null

    /**
     * This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("genderIdentity")
    @JsonPropertyDescription("This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.")
    var genderIdentity: CodeableConcept? = null

    /**
     * This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("religion")
    @JsonPropertyDescription("This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.")
    var religion: CodeableConcept? = null

    /**
     * Whether the Patient's record is in active use
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("isActive")
    @JsonPropertyDescription("Whether the Patient's record is in active use")
    var isActive: Boolean? = null

    /**
     * Patient's name
     * (Required)
     *
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Patient's name")
    var name: List<HumanName>? = ArrayList()

    /**
     * Patient's contact details
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("telecom")
    @JsonPropertyDescription("Patient's contact details")
    var telecom: List<RoninContactPoint>? = ArrayList()

    /**
     * Patient's Administrative Gender
     * (Required)
     *
     */
    @JsonProperty("gender")
    @JsonPropertyDescription("Patient's Administrative Gender")
    var gender: String? = null

    /**
     * Patient's birthdate
     * (Required)
     *
     */
    @JsonProperty("birthDate")
    @JsonPropertyDescription("Patient's birthdate")
    var birthDate: LocalDate? = null

    @Nullable
    @JsonProperty("deceased")
    var deceased: Deceased? = null

    /**
     * Patient's home address
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("address")
    @JsonPropertyDescription("Patient's home address")
    var address: List<Address>? = ArrayList()

    /**
     * This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("maritalStatus")
    @JsonPropertyDescription("This is a CodeableConcept with an extracted emrText attribute that dictates the correct value to represent in an EMR context.")
    var maritalStatus: CodeableConcept? = null

    @Nullable
    @JsonProperty("multipleBirth")
    var multipleBirth: MultipleBirth? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("photo")
    var photo: List<Attachment>? = ArrayList()

    /**
     * Point of Contact for the Patient
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("contact")
    @JsonPropertyDescription("Point of Contact for the Patient")
    var contact: List<Contact>? = ArrayList()

    /**
     * Languages that may be used to communicate with the Patient
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("communication")
    @JsonPropertyDescription("Languages that may be used to communicate with the Patient")
    var communication: List<Communication>? = ArrayList()

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("generalPractitioner")
    var generalPractitioner: ResourceReference? = null

    /**
     *
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("managingOrganization")
    var managingOrganization: ResourceReference? = null

    /**
     * Link to another patient record that refers to this Patient
     * (Can be null)
     *
     */
    @Nullable
    @JsonProperty("link")
    @JsonPropertyDescription("Link to another patient record that refers to this Patient")
    var link: Link? = null

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("createdAt")
    var createdAt: Instant? = null

    /**
     *
     * (Required)
     *
     */
    @JsonProperty("updatedAt")
    var updatedAt: Instant? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any>? = LinkedHashMap()

    /**
     * No args constructor for use in serialization
     *
     */
    constructor() {}

    /**
     *
     * @param deceased
     * @param ethnicity
     * @param gender
     * @param link
     * @param isActive
     * @param genderIdentity
     * @param createdAt
     * @param contact
     * @param generalPractitioner
     * @param birthSex
     * @param telecom
     * @param id
     * @param communication
     * @param fhirId
     * @param updatedAt
     * @param address
     * @param race
     * @param identifiers
     * @param resourceVersion
     * @param mrn
     * @param photo
     * @param birthDate
     * @param religion
     * @param multipleBirth
     * @param managingOrganization
     * @param tenantId
     * @param name
     * @param maritalStatus
     */
    constructor(
        id: String?,
        fhirId: String?,
        tenantId: String?,
        mrn: String?,
        resourceVersion: String?,
        identifiers: List<Identifier>?,
        race: CodeableConcept?,
        ethnicity: CodeableConcept?,
        birthSex: ValueCode?,
        genderIdentity: CodeableConcept?,
        religion: CodeableConcept?,
        isActive: Boolean?,
        name: List<HumanName>?,
        telecom: List<RoninContactPoint>?,
        gender: String?,
        birthDate: LocalDate?,
        deceased: Deceased?,
        address: List<Address>?,
        maritalStatus: CodeableConcept?,
        multipleBirth: MultipleBirth?,
        photo: List<Attachment>?,
        contact: List<Contact>?,
        communication: List<Communication>?,
        generalPractitioner: ResourceReference?,
        managingOrganization: ResourceReference?,
        link: Link?,
        createdAt: Instant?,
        updatedAt: Instant?
    ) : super() {
        this.id = id
        this.fhirId = fhirId
        this.tenantId = tenantId
        this.mrn = mrn
        this.resourceVersion = resourceVersion
        this.identifiers = identifiers
        this.race = race
        this.ethnicity = ethnicity
        this.birthSex = birthSex
        this.genderIdentity = genderIdentity
        this.religion = religion
        this.isActive = isActive
        this.name = name
        this.telecom = telecom
        this.gender = gender
        this.birthDate = birthDate
        this.deceased = deceased
        this.address = address
        this.maritalStatus = maritalStatus
        this.multipleBirth = multipleBirth
        this.photo = photo
        this.contact = contact
        this.communication = communication
        this.generalPractitioner = generalPractitioner
        this.managingOrganization = managingOrganization
        this.link = link
        this.createdAt = createdAt
        this.updatedAt = updatedAt
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
        sb.append(Patient::class.java.name).append('@').append(Integer.toHexString(System.identityHashCode(this)))
            .append('[')
        sb.append("id")
        sb.append('=')
        sb.append(if (id == null) "<null>" else id)
        sb.append(',')
        sb.append("fhirId")
        sb.append('=')
        sb.append(if (fhirId == null) "<null>" else fhirId)
        sb.append(',')
        sb.append("tenantId")
        sb.append('=')
        sb.append(if (tenantId == null) "<null>" else tenantId)
        sb.append(',')
        sb.append("mrn")
        sb.append('=')
        sb.append(if (mrn == null) "<null>" else mrn)
        sb.append(',')
        sb.append("resourceVersion")
        sb.append('=')
        sb.append(if (resourceVersion == null) "<null>" else resourceVersion)
        sb.append(',')
        sb.append("identifiers")
        sb.append('=')
        sb.append(if (identifiers == null) "<null>" else identifiers)
        sb.append(',')
        sb.append("race")
        sb.append('=')
        sb.append(if (race == null) "<null>" else race)
        sb.append(',')
        sb.append("ethnicity")
        sb.append('=')
        sb.append(if (ethnicity == null) "<null>" else ethnicity)
        sb.append(',')
        sb.append("birthSex")
        sb.append('=')
        sb.append(if (birthSex == null) "<null>" else birthSex)
        sb.append(',')
        sb.append("genderIdentity")
        sb.append('=')
        sb.append(if (genderIdentity == null) "<null>" else genderIdentity)
        sb.append(',')
        sb.append("religion")
        sb.append('=')
        sb.append(if (religion == null) "<null>" else religion)
        sb.append(',')
        sb.append("isActive")
        sb.append('=')
        sb.append(if (isActive == null) "<null>" else isActive)
        sb.append(',')
        sb.append("name")
        sb.append('=')
        sb.append(if (name == null) "<null>" else name)
        sb.append(',')
        sb.append("telecom")
        sb.append('=')
        sb.append(if (telecom == null) "<null>" else telecom)
        sb.append(',')
        sb.append("gender")
        sb.append('=')
        sb.append(if (gender == null) "<null>" else gender)
        sb.append(',')
        sb.append("birthDate")
        sb.append('=')
        sb.append(if (birthDate == null) "<null>" else birthDate)
        sb.append(',')
        sb.append("deceased")
        sb.append('=')
        sb.append(if (deceased == null) "<null>" else deceased)
        sb.append(',')
        sb.append("address")
        sb.append('=')
        sb.append(if (address == null) "<null>" else address)
        sb.append(',')
        sb.append("maritalStatus")
        sb.append('=')
        sb.append(if (maritalStatus == null) "<null>" else maritalStatus)
        sb.append(',')
        sb.append("multipleBirth")
        sb.append('=')
        sb.append(if (multipleBirth == null) "<null>" else multipleBirth)
        sb.append(',')
        sb.append("photo")
        sb.append('=')
        sb.append(if (photo == null) "<null>" else photo)
        sb.append(',')
        sb.append("contact")
        sb.append('=')
        sb.append(if (contact == null) "<null>" else contact)
        sb.append(',')
        sb.append("communication")
        sb.append('=')
        sb.append(if (communication == null) "<null>" else communication)
        sb.append(',')
        sb.append("generalPractitioner")
        sb.append('=')
        sb.append(if (generalPractitioner == null) "<null>" else generalPractitioner)
        sb.append(',')
        sb.append("managingOrganization")
        sb.append('=')
        sb.append(if (managingOrganization == null) "<null>" else managingOrganization)
        sb.append(',')
        sb.append("link")
        sb.append('=')
        sb.append(if (link == null) "<null>" else link)
        sb.append(',')
        sb.append("createdAt")
        sb.append('=')
        sb.append(if (createdAt == null) "<null>" else createdAt)
        sb.append(',')
        sb.append("updatedAt")
        sb.append('=')
        sb.append(if (updatedAt == null) "<null>" else updatedAt)
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
        result = result * 31 + if (deceased == null) 0 else deceased.hashCode()
        result = result * 31 + if (ethnicity == null) 0 else ethnicity.hashCode()
        result = result * 31 + if (gender == null) 0 else gender.hashCode()
        result = result * 31 + if (link == null) 0 else link.hashCode()
        result = result * 31 + if (isActive == null) 0 else isActive.hashCode()
        result = result * 31 + if (genderIdentity == null) 0 else genderIdentity.hashCode()
        result = result * 31 + if (createdAt == null) 0 else createdAt.hashCode()
        result = result * 31 + if (contact == null) 0 else contact.hashCode()
        result = result * 31 + if (generalPractitioner == null) 0 else generalPractitioner.hashCode()
        result = result * 31 + if (birthSex == null) 0 else birthSex.hashCode()
        result = result * 31 + if (telecom == null) 0 else telecom.hashCode()
        result = result * 31 + if (id == null) 0 else id.hashCode()
        result = result * 31 + if (communication == null) 0 else communication.hashCode()
        result = result * 31 + if (fhirId == null) 0 else fhirId.hashCode()
        result = result * 31 + if (updatedAt == null) 0 else updatedAt.hashCode()
        result = result * 31 + if (address == null) 0 else address.hashCode()
        result = result * 31 + if (race == null) 0 else race.hashCode()
        result = result * 31 + if (identifiers == null) 0 else identifiers.hashCode()
        result = result * 31 + if (resourceVersion == null) 0 else resourceVersion.hashCode()
        result = result * 31 + if (mrn == null) 0 else mrn.hashCode()
        result = result * 31 + if (photo == null) 0 else photo.hashCode()
        result = result * 31 + if (birthDate == null) 0 else birthDate.hashCode()
        result = result * 31 + if (religion == null) 0 else religion.hashCode()
        result = result * 31 + if (multipleBirth == null) 0 else multipleBirth.hashCode()
        result = result * 31 + if (managingOrganization == null) 0 else managingOrganization.hashCode()
        result = result * 31 + if (tenantId == null) 0 else tenantId.hashCode()
        result = result * 31 + if (name == null) 0 else name.hashCode()
        result = result * 31 + if (additionalProperties == null) 0 else additionalProperties.hashCode()
        result = result * 31 + if (maritalStatus == null) 0 else maritalStatus.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is Patient == false) {
            return false
        }
        val rhs = other
        return (deceased === rhs.deceased || deceased != null && deceased == rhs.deceased) && (ethnicity === rhs.ethnicity || ethnicity != null && ethnicity == rhs.ethnicity) && (gender === rhs.gender || gender != null && gender == rhs.gender) && (link === rhs.link || link != null && link == rhs.link) && (isActive === rhs.isActive || isActive != null && isActive == rhs.isActive) && (genderIdentity === rhs.genderIdentity || genderIdentity != null && genderIdentity == rhs.genderIdentity) && (createdAt === rhs.createdAt || createdAt != null && createdAt == rhs.createdAt) && (contact === rhs.contact || contact != null && contact == rhs.contact) && (generalPractitioner === rhs.generalPractitioner || generalPractitioner != null && generalPractitioner == rhs.generalPractitioner) && (birthSex === rhs.birthSex || birthSex != null && birthSex == rhs.birthSex) && (telecom === rhs.telecom || telecom != null && telecom == rhs.telecom) && (id === rhs.id || id != null && id == rhs.id) && (communication === rhs.communication || communication != null && communication == rhs.communication) && (fhirId === rhs.fhirId || fhirId != null && fhirId == rhs.fhirId) && (updatedAt === rhs.updatedAt || updatedAt != null && updatedAt == rhs.updatedAt) && (address === rhs.address || address != null && address == rhs.address) && (race === rhs.race || race != null && race == rhs.race) && (identifiers === rhs.identifiers || identifiers != null && identifiers == rhs.identifiers) && (resourceVersion === rhs.resourceVersion || resourceVersion != null && resourceVersion == rhs.resourceVersion) && (mrn === rhs.mrn || mrn != null && mrn == rhs.mrn) && (photo === rhs.photo || photo != null && photo == rhs.photo) && (birthDate === rhs.birthDate || birthDate != null && birthDate == rhs.birthDate) && (religion === rhs.religion || religion != null && religion == rhs.religion) && (multipleBirth === rhs.multipleBirth || multipleBirth != null && multipleBirth == rhs.multipleBirth) && (managingOrganization === rhs.managingOrganization || managingOrganization != null && managingOrganization == rhs.managingOrganization) && (tenantId === rhs.tenantId || tenantId != null && tenantId == rhs.tenantId) && (name === rhs.name || name != null && name == rhs.name) && (additionalProperties === rhs.additionalProperties || additionalProperties != null && additionalProperties == rhs.additionalProperties) && (maritalStatus === rhs.maritalStatus || maritalStatus != null && maritalStatus == rhs.maritalStatus)
    }
}
