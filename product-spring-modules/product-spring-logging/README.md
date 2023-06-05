# Product Spring Logging
Library used to manage and setup Logging.

By default, logstash is setup to encode logs not locally in json for DataDog. Local profile and test profile's
default to standard logging to the console.

## Configuration
Import the library and logging should be set up automatically.

If the service utilizes product-spring-common, product-spring-web-starter, or product-spring-webflux-starter, then it 
should be applied automatically unless overridden with service implementations.

