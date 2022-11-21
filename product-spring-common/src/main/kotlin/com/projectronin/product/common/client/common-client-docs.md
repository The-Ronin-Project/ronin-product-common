# Package com.projectronin.product.common.client
<!-- todo: what should file name be?? -->
<!-- todo: appropriate place for this file to live -->

# Service Clients
The existing AbstractServiceClient is available to be extended to create custom kotlin clients 
used make API requests against other Ronin Kotlin services. 

# Examples
## Basic example
Below is a simple example of creation of a PatientClient 
(which can be used for CRUD operations against the Clinical Data Service)
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
4. authBroker is used to supply Authorization Header value for given requests
5. client is OPTIONAL, when want to supply custom client.
6. this like is a call to the constructor of the Abstract class
7. value to be used as value for 'User-Agent' request header (primarily used for log tracking)
8. 'get' function generates the full request URL then calls 'executeGet' in the abstract class
   1. Note 1: the response will '_<u>automatically</u>_' be deserialized into a Patient object
   2. Note 2: _**it is assumed that the structure of the `Patient` class is compatible with the expected response.**_
9. 'create' is an example of submitting a POST call to create an object
   1. executePost details 
      1. 1st parameter is the full request url
      2. 2nd parameter is the object to be sent as the POST request body.  <u>_The object will be automatically serialized into a JSON String_</u>
10. 'delete' call, generates full request url and submits a DELETE call.

Other Details:
1. an error on any method call with throw a ServiceClientException (**_including a 4xx/5xx http status response_**)
   1. the exception will contain extra information about the error as desired.
2. All requests are made with a set of 'default headers' (which can be altered if desired)
3. If ever the authBroker returns an empty string (""), then no Authorization request header will be added.
4. The '@throws' annotation on the methods are not strictly required, it is for being explicit about why kind of exception can be thrown.
 
## Permutation Examples
### Customizing request headers
The base client has a method `getRequestHeaderMap` which can be overridden if you want to customize the request headers
<br>EXAMPLE:
```kotlin
override fun getRequestHeaderMap(method: String, requestUrl: String, bearerAuthToken: String): MutableMap<String, String> {
    // grab a map of all the default request headers from super class,
    //    then append an additional 'Host' header
  return super.getRequestHeaderMap(method, requestUrl, bearerAuthToken)
      .apply { 
          put(HttpHeaders.HOST, "myHost")
      } 
}
```

### Getting the actual response body
If you want a method to return the raw response body (instead of an object), just use a return type of `String`
<br>EXAMPLE:
```kotlin
//  rewrite the existing 'getPatient' method above to return raw response (instead of a Patient object)
@Throws(ServiceClientException::class)
fun getAsString(id: String): String { 
    return executeGet("$baseUrl$PATIENT_PATH/$id")
}
```

### Getting the actual response as a generic map
Getting the response deserialized into a generic map can be done like below.  
Note that it is literally just a different kind of return type
<br>EXAMPLE:
```kotlin
@Throws(ServiceClientException::class)
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
        val serviceResponse: ServiceResponse = executeRawGet("$baseUrl$PATIENT_PATH/$id")
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
       val serviceResponse: ServiceResponse = executeRawGet("$baseUrl$PATIENT_PATH/$id", false)
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
// example using existing factory to create an okHttpClient with a custom connection timeout value
val configMap: Map<String, Any> = mapOf("connection.timeout" to 10000)
val httpClient = StdHttpClientFactory.createClient(configMap)
val patientClient = PatientClient(hostUrl, authBroker, httpClient)
```
_NOTE_:  the 'value' used for the connectionTimeout above may be an `Integer`, `Long`, `String`, or `Duration`
