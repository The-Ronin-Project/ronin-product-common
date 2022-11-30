# Package com.projectronin.product.common.client
<!-- todo: what should file name be?? -->
<!-- todo: appropriate place for this file to live -->

# Service Clients
The existing AbstractServiceClient is available to be extended to create custom kotlin clients 
used make API requests against other Ronin Kotlin services. 

# Examples
## Basic Client Example - Implementation
Below is a simple example of creation of a PatientClient 
(which can be used for `Patient` CRUD operations against the Clinical Data Service)
```kotlin
private const val PATIENT_PATH = "api/patient"  // (__1__)
class PatientClient(        // (__2__)
    hostUrl: String,        // (__3__)
    authBroker: AuthBroker, // (__4__)
    client: OkHttpClient = defaultOkHttpClient()  // (__5__)
) :
    AbstractServiceClient(hostUrl, authBroker, client) {  // (__6__)
    override fun getUserAgentValue(): String {
        return "PatientClient/1.0.0"  // (__7__)
    }

    @Throws(ServiceClientException::class)
    fun get(id: String): Patient {
        return executeGet("$baseUrl$PATIENT_PATH/$id")  // (__8__)
    }

    @Throws(ServiceClientException::class)
    fun create(patient: Patient): Patient {
        return executePost("$baseUrl$PATIENT_PATH", patient)  // (__9__)
    }
    
    @Throws(ServiceClientException::class)
    fun delete(id: String) {
        executeDelete("$baseUrl$PATIENT_PATH/$id")  // (__10__)
    }
}
```
1. this in the base endpoint path to be appended to the base url.
2. client class name
3. hostUrl is constructor param for base host url (i.e. 'https://myhost.com')
4. authBroker is used to supply an Authorization Header value for given requests
5. client is OPTIONAL, when want to supply custom client.
6. this is a call to the constructor of the Abstract class
7. value to be used as value for 'User-Agent' request header (primarily used for log tracking)
8. 'get' function generates the full request URL then calls 'executeGet' in the abstract class
   1. Note 1: the response will '_<u>automatically</u>_' be deserialized into a Patient object
   2. Note 2: _**it is assumed that the structure of the `Patient` class is compatible with the expected response.**_
9. 'create' is an example of submitting a POST call to create an object
   1. executePost details 
      1. 1st parameter is the full request url
      2. 2nd parameter is the object to be sent as the POST request body.  <u>_**The object will be automatically serialized into a JSON String**_</u>
10. 'delete' call, generates full request url and submits a DELETE call.

Other Details:
1. an error on any method call with throw a ServiceClientException (**_including a 4xx/5xx http status response_**)
   1. the exception will contain extra information about the error as desired.
2. All requests are made with a set of 'default headers' (which can be altered/overridden as needed)
3. If ever the authBroker returns an empty string (""), then no Authorization request header will be added.
4. The '@throws' annotation on the methods are not strictly required, it is for being explicit about why kind of exception can be thrown.
5. If you pass in a String as a POST body, that value will be used directly  (no object serialization will be attempted)
 
## Basic Client Example - Usage
Simple code example of how to actually use the example `PatientClient` implementation above
```kotlin
fun main(args: Array<String>) {
   val hostName = "https://clinical-data.dev.projectronin.io"
   val authBroker = PassThruAuthBroker("_my_seki_token_")
   val patientClient = PatientClient(hostName, authBroker)

   // fetch a patient
   val fetchedPatient = patientClient.get("_patient_id_")
   
   // create a patient
   val patientToCreate = Patient(
      displayName = "Robert Paulson",
      tenantId = "mdaocFake",
      mrn = "12346789",
      birthSex = "M",
      birthDate = LocalDate.of(1950, 4, 29)
   )
   
   // call create (and get back the createdPatient, which will have an id)    
   val createdPatient = patientClient.create(patientToCreate)
}
```

## Permutation Examples
### Customizing request headers
The base client has a method `getRequestHeaderMap` which can be overridden if you want to customize the request headers
<br>EXAMPLE:
```kotlin
override fun generateRequestHeaderMap(method: String, requestUrl: String, extraHeaderMap: Map<String, String>): MutableMap<String, String> {
   // include another 'special' header for all requests
   return super.generateRequestHeaderMap(method, requestUrl, extraHeaderMap + mapOf("X-Special" to "abcd"))
}
```

### Getting the actual response body
If you want a method to return the raw response body (instead of an object), just use a return type of `String`
<br>EXAMPLE:
```kotlin
//  rewrite the existing 'getPatient' method above to return raw response (instead of a Patient object)
fun getAsString(id: String): String { 
    return executeGet("$baseUrl$PATIENT_PATH/$id")
}
```

### Getting the actual response as a generic map
Getting the response deserialized into a generic map can be done like below.  
Note that it is literally just a different kind of return type
<br>EXAMPLE:
```kotlin
fun getAsMap(id: String): Map<String, Any> {
   return executeGet("$baseUrl$PATIENT_PATH/$id")
}
```

### Accessing the raw response details
if want access to the response body, response headers, httpStatus code, 
then call one of the 'raw' methods are available for use.
<br>EXAMPLE:
```kotlin
class PatientClient(  ) {
    // ... 
    fun mySpecialMethod(id: String) {
        val serviceResponse: ServiceResponse = executeRequest(makeGetRequest("$baseUrl$PATIENT_PATH/$id"))
        // use values from the serviceResponse as necessary.
    }
}
```

### Make Get call but do NOT throw an Exception if a 4xx/5xx error occurs.
By default, any httpStatus code of 4xx or 5xx will result in an exception.  
If you want to access the raw response regardless of what httpStatus code was returned, 
then you can call the custom raw get overriding the default parameter to not thrown exception on httpError
<br>EXAMPLE
```kotlin
class PatientClient(  ) {
    // ... 
    fun anotherSpecialMethod(id: String) { 
       // extra 'false' param signals to not throw on 4xx/5xx error.
       //    However, it IS possible for an exception to still be thrown for other error types (e.g. "UnknownHost")
       val serviceResponse: ServiceResponse = executeRequest(makeGetRequest(url = "$baseUrl$PATIENT_PATH/$id", shouldThrowOnStatusError = false))

       if (serviceResponse.httpStatus.isError) {
          // do special handling for a http error response here.
       }
       // .... 
    }
}
```

### Customizing the 'timeout' on the inner client
When a "defaultClient" is created on the constructor, it will have a preset values for 'connection timeouts'.
(so a call doesn't appear to be stuck indefinitely).  When creating a serviceClient,
it is possible to provider a custom httpClient that has custom timeout values.

```kotlin
// example using existing factory to create an okHttpClient with a custom connection timeout value of 10 seconds
val configMap: Map<String, Any> = mapOf("connection.timeout" to 10000)
val httpClient = StdHttpClientFactory.createClient(configMap)
val patientClient = PatientClient(hostUrl, authBroker, httpClient)
```
_NOTE_:  the 'value' used for the connectionTimeout above may be an `Integer`, `Long`, `String`, or `Duration`
1. The `String` value can be represented in multiple forms, such as: "1m" (1 minute), "60s" (60 seconds), or even special Duration string format: "PT5M" (5 minutes)
2. When an `Integer` or `Long` value is specified, it is assumed to be _milliseconds_.

## Other Implementation Examples
Examples below are how client classes could be created for other Kotlin services.
<br> **_NOTE_**: these examples are approximate, and might not represent the current implementation of a given service.

### Questionnaire Client Example
```kotlin
private const val QUESTIONNAIRE_PATH = "api/v1/questionnaire"
class QuestionnaireClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "QuestionnaireClient/1.0.0"
    }

    fun createAssignQuestionnaire(assignmentRequestContext: AssignmentRequestContext): QuestionnaireAssignmentResponse {
        return executePost("$baseUrl$QUESTIONNAIRE_PATH", assignmentRequestContext)
    }

    fun getQuestionnaireState(assignmentId: UUID): QuestionnaireAssignmentStateResponse {
        return executeGet("$baseUrl$QUESTIONNAIRE_PATH/$assignmentId")
    }

    fun submitAnswers(assignmentId: UUID, answerSubmission: AnswerSubmission) : String {
        // this particular method requires an extra 'match' header to be set.
        //    just always set to 'true' for this example
        return executePost("$baseUrl$QUESTIONNAIRE_PATH/$assignmentId", answerSubmission, mapOf(HttpHeaders.IF_MATCH to "true"))
    }
}
```
### Audit Service Client Example
```kotlin
private const val AUDIT_PATH = "api/audit"
class AuditClient(
    hostUrl: String,
    authBroker: AuthBroker,
    client: OkHttpClient = defaultOkHttpClient()
) :
    AbstractServiceClient(hostUrl, authBroker, client) {
    override fun getUserAgentValue(): String {
        return "AuditClient/1.0.0"
    }

    fun get(id: UUID): Audit {
        return executeGet("$baseUrl$AUDIT_PATH/$id")
    }

    fun create(audit: Audit): UUID {
        // grab response as a map, then only return the 'id' value
        val keyValueMap: Map<String, Any> = executePost("$baseUrl$AUDIT_PATH", audit)
        return UUID.fromString(keyValueMap["id"]?.toString() ?: "")
    }
}
```
