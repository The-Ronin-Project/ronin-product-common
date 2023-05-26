# Product Spring Telemetry
Library used to manage telemetry

## Configuration
Include Telemetry Config class in Spring configuration
```kotlin
import com.projectronin.product.telemetry.config.TelemetryConfig

@ComponentScan("com.projectronin.product.common.config")
@Configuration
@EnableConfigurationProperties(AssetsProperties::class)
@Import(AuditConfig::class, TelemetryConfig::class)
class SharedConfigurationReference
```

### Customizing IgnoreInterceptor
If there are traces that need to be ignored, more than the default actuator endpoints, the interceptor can be configured
in the following manner.

```yaml
ronin:
  product:
    telemetry:
      ignoreTraces:
        - GET /actuator
```
