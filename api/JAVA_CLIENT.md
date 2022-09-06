# ManageMC API: Java Client

This guide assumes that you have already completed the preliminary steps in our [development guide](../docs/DEVELOPMENT_GUIDE.md).

## Dependency

We have written a wrapper around the generated Java client to make it handle token refreshing logic, thread safety, and other things. The wrapper is strongly recommended and all of our docs will be tailored to it:

```xml
<project>
    <dependencies>
        <!-- ManageMC API client wrapper -->
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

At your own peril, you could choose to work directly with the generated client instead:

```xml
<!-- ManageMC API client -->
<dependency>
    <groupId>com.managemc.api</groupId>
    <artifactId>managemc-api</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage

initialize the client provider like this:

```java
// example only; don't actually hard code your API keys
Keys keys = new Keys("your-public-key", "your-private-key");

ClientProvider provider = ClientProvider.demo(keys, "your-server-group");
```

In the example above, we configure the client provider to make requests to our `demo` API. `local` and `production` are also available. Please see our [environment guide](../docs/ENVIRONMENTS.md) to decide which is appropriate for your use case.

The generated client is not thread-safe by default. The `ClientProvider` class handles concurrency issues for you, but only if you use it correctly! Create one instance of `ClientProvider` in your application and request a new client instance every time you make an API request.

### Good Example

```java
// store one instance of ClientProvider, but never any instances of its clients
private final ClientProvider provider;

public MyClass(ClientProvider provider) {
  this.provider = provider;
}

public void exampleRequest() throws ApiException {
  provider.externalServer().getPlayersApi().fetchPlayer("hypixel");
}
```

### Bad Example

```java
// this is tempting but will break thread safety if you aren't careful
private final ExternalServerClient api;

public MyClass(ClientProvider provider) {
  this.api = provider.externalServer();
}

public void exampleRequest() throws ApiException {
  // less code to write, but no thread safety!
  api.getPlayersApi().fetchPlayer("hypixel");
}
```
