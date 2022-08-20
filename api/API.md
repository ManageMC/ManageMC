# ManageMC API

This API contract describes how to interact with nearly all public-facing services provided by ManageMC. It contains over 100 endpoints and powers our plugins, website, and mobile app (coming soon). Customers are encouraged to use it to enhance their own plugins, website, and other projects.

Copy the contents of [api.yml](https://raw.githubusercontent.com/ManageMC/ManageMC/main/api/api.yml) and paste them into the [OpenAPI editor](https://editor.swagger.io/) to browse through our documentation more easily. Every endpoint represents an HTTP request that can be made, all of the parameters it supports, what data it will return to the consumer, and what error conditions the consumer may wish to handle.

## Development

### Generated Clients

We use [openapi-generator](https://github.com/OpenAPITools/openapi-generator), which reads the API schema we have defined and generates code for consuming the API in many popular languages. We publish packages for the following languages in this project for you to use in your own code:

- Java
- Ruby

### Java Dependency

Add our API as a dependency to your Maven project:

```xml
<project>
    <dependencies>
        <!-- ManageMC API client -->
        <dependency>
            <groupId>com.managemc.api.wrapper</groupId>
            <artifactId>managemc-api-wrapper</artifactId>
            <version>{latest-version-goes-here}</version>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>github</id>
            <name>ManageMC</name>
            <url>https://maven.pkg.github.com/ManageMC/ManageMC</url>
        </repository>
    </repositories>
</project>
```
