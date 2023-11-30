# product-spring-openapi-generated

When included in your spring service, publishes openapi documentation on your services.

## Usage

If you want springdoc to generate the documentation from your controllers, and you intend to fully annotate them, nothing needs to be done but to access them at the right URL:

`http://localhost:8080/swagger-ui.html`

It is preferable, however, that you publish the contract that you're generating your controllers and models from.  To do so, add this to your `application.yml`:

```yaml
ronin:
  product:
    swagger:
      generated: false
springdoc:
  swagger-ui:
    urls-primary-name: "Descriptive Name for Contract"
    urls:
      - url: /v3/api-docs/<contract project name>/v<N>.yaml
        name: "Descriptive Name for Contract"
```

This assumes that you're using the latest 2.3.9+ `ronin-contract-openapi-plugin` to generate your API code in the contract repository.
