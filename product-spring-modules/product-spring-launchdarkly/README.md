# Product Spring LaunchDarkly
A [LaunchDarkly Java SDK](https://docs.launchdarkly.com/sdk/server-side/java) wrapper implementation for feature-flags.

## Adding to a project
First you need to set up a LaunchDarkly project and environment SDK keys under Account Settings > Projects > Environments. You should have at least 3 environments (Dev, Stage, Production). 

See [Confluence](https://projectronin.atlassian.net/wiki/spaces/ENG/pages/736493584/LaunchDarkly+Feature+Flags)

Once you've create your project and SDK keys you need to move your SDK key values to Vault secret store for each environment. 
Then you can add those secret key values to your ronin-chart values for each environment, making note of the environment variable name your using for the SDK key.


### ronin-charts config
Example base values file
```yaml
    - name: LAUNCH_DARKLY_OFFLINE               # Optional: defaults to false
      valueFrom:
        configMapKeyRef:
          name: [service]-config
          key: LAUNCH_DARKLY_OFFLINE
    - name: LAUNCH_DARKLY_DIAGNOSTICS_OPT_OUT   # Optional: defaults to true
      valueFrom:
        configMapKeyRef:
          name: [service]-config
          key: LAUNCH_DARKLY_DIAGNOSTICS_OPT_OUT
```

Example environment specific values
```yaml
environment: dev
...
configMap:
  ...
  data:
    LAUNCH_DARKLY_OFFLINE: false
    LAUNCH_DARKLY_DIAGNOSTICS_OPT_OUT: false
...
valssecret:
  enabled: true
  data:
    LAUNCH_DARKLY_SDK_KEY:
      ref: ref+vault://product_engineering/[service vault location]/dev/LAUNCH_DARKLY_SDK_KEY
```

### Example application.yaml
```yaml
ronin:
  product:
    launch-darkly:
      client-sdk-key: ${LAUNCH_DARKLY_SDK_KEY}                    # Secret LDClient SDK key for the current environment, if missing or set to false will use testClient
      offline: ${LAUNCH_DARKLY_OFFLINE:false}                     # Optional: set offline mode if you don't want to send events to the client. All flag values will return default.
      diagnosticOptOut: ${LAUNCH_DARKLY_DIAGNOSTIC_OPT_OUT:true}  # Optional: only set to false if you want to share metrics with LaunchDarkly
```

### settings.gradle.kts
Make sure you are on a new enough version of ronin-product-common. Greater than 2.6.0
```
    versionCatalogs {
        create("libs") {
            from("com.projectronin:ronin-product-common:2.6.1")
        }
    }
```

### [project-name]/build.gradle.kts
Include the dependency.
```
    implementation(libs.product.launchdarkly)
```

### SharedConfigurationReference
Need to make sure that the proper config classes are included. You can trust the magic that is ComponentScan, or directly Import the following config.
```
@Import(LaunchDarklyConfig::class)
```

## Usage
Usage of the FeatureFlagService is fairly simple. 
Given a flag key, a context key, and a default value it will return the current value for that flag and any variation based on the context.
If there is no match on flag key or context it will return the default.
LDContext is a replacement for v5 LDUser and the default ContextKind is USER.

Keep in mind if the LDClient is in offline mode it will return the last cached value of the flag, or the default value if initially configured in offline mode.

```kotlin
@Autowired
private lateinit var featureFlagService: FeatureFlagService

// Pass a String flag key, a context key, and a default value. The return type is determined by the default value's type.
val booleanFlag: Boolean = featureFlagService.flag("my-bool-flag", "user1", false)

val intFlag: Int = featureFlagService.flag("my-int-flag", "user1", 1)

val stringFlag: String = featureFlagService.flag("my-string-flag", "user1", "default")
```

## Testing
In tests these easiest thing you can do is create a mock bean of the FeatureFlagService and return whatever value you want for the flag calls.

Alternatively, if the test and local application properties do not define a `ronin.product.launch-darkly.client-sdk-key` the LaunchDarklyConfig should instantiate the testLaunchDarklyClient().
The advantage of the testLaunchDarklyClient is that you can use the TestData source to flip flags directly and test variations without having to mock them.
The [TestData](https://docs.launchdarkly.com/sdk/features/test-data-sources) datasource has a DSL that lets you modify the returned results and variations.

Example test
```kotlin
@Autowired
lateinit var ldConfig: LaunchDarklyConfig

// The testData bean is used in configuring the testLaunchDarklyClient() which will be used as the FeatureFlagService implementation if clientSdkKey is missing
val testData = ldConfig.testData()

// sets the boolean flag "my-flag" to true for all users and contexts
testData.run {
    update(flag("my-flag").booleanFlag().variationForAll(true))
}

```
