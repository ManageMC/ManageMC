# ManageMC API

This API contract describes how to interact with nearly all public-facing services provided by ManageMC. It contains over 100 endpoints and powers our plugins, website, and mobile app (coming soon). Customers are encouraged to use it to enhance their own plugins, website, and other projects.

## Documentation

Copy the contents of [api.yml](https://raw.githubusercontent.com/ManageMC/ManageMC/main/api/api.yml) and paste them into the [OpenAPI editor](https://editor.swagger.io/) to browse through our documentation more easily. Every endpoint represents an HTTP request that can be made, all of the parameters it supports, what data it will return to the consumer, and what error conditions the consumer may wish to handle.

Unfortunately, you will need to consult `api.yml` directly for things like the supported authentication method, which won't show up in the editor. Only endpoints with the following `security` elements may be consumed by customers:

- no security elements at all (which means the API is public and requires no authentication)
- `external_minecraft`: available to Minecraft servers and (perhaps misleadingly) non-Minecraft applications without specific users
- `user_minecraft`: available to players on Minecraft servers

If you would like to use an endpoint that doesn't currently allow requests from Minecraft, please reach out for help. It is usually not difficult to make endpoints available.

## Generated Clients

We use [openapi-generator](https://github.com/OpenAPITools/openapi-generator), which generates client code the for API we have defined. We maintain packages for the following client languages in this project for you to use in your own code:

- Java ([guide](JAVA_CLIENT.md))
- Ruby ([guide](RUBY_CLIENT.md))

Although we do not currently offer a JavaScript/TypeScript package, our API does support CORS and thus can be consumed directly from a web browser.
