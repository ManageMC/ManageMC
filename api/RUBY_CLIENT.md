# ManageMC API: Ruby Client

Unlike working with our [Java client](JAVA_CLIENT.md), working with the Ruby client means you may need to address thread safety and refreshing logic yourself, since we haven't written a wrapper library (yet?).

## Prerequisites

- You have installed Ruby on your system and are comfortable working with it
- You are familiar with RubyGems/Bundler and know how to install gems

## Dependency

The client is available as a gem on GitHub Packages. You will need to authenticate with them in order to use it. The first two steps here are identical to those found in our general-purpose [development guide](../docs/DEVELOPMENT_GUIDE.md):

1. Create a GitHub account if you don't already have one
2. Go [here](https://github.com/settings/tokens) and generate a new token. Set the expiration to `No Expiration` and select only the `read:packages` box. After you generate the token, keep the page open so that you can copy the token later. You may use the same token for Ruby and Java development.
3. Add the following to your bundler config (typically found at `~/.bundle/config`):

   ```yaml
   BUNDLE_HTTPS://RUBYGEMS__PKG__GITHUB__COM/MANAGEMC/: "YourGitHubUsername:ghp_therestofyourtoken"
   ```

4. Check for the latest version of our client library (published [here](https://github.com/ManageMC/ManageMC/packages/1605055)) and add it to your `Gemfile`:

   ```ruby
   source 'https://rubygems.pkg.github.com/managemc' do
     gem 'managemc_api', 'latest-version-here'
   end
   ```

5. Run `bundle install` and you should be all set.

## Usage

Our API is broken up into sections, denoted by the `tags` field in [api.yml](../api/api.yml). Initializing an API is simple, but you may need to browse the generated source code if you need non-default configuration:

```ruby
require 'managemc_api'

AUTHENTICATION_API = ManagemcApi::AuthenticationApi.new
```

You can configure the base URL and auth token by setting the `server_index` and `access_token` properties as follows:

```ruby
ManagemcApi.configure do |config|
  # we don't think these fields actually do anything but just to be safe...
  config.scheme = 'https'
  config.host = 'api-demo.managemc.com'

  # tell the client which server to use
  config.server_index = 1

  # set the access token
  config.access_token = 'just-your-token-no-http-headers'
end
```

The `server_index` corresponds with the `servers` block in [api.yml](../api/api.yml). We will never change the indices of existing elements, but we may add new ones.

```yaml
servers:
  # index 0 (default, which you probably do not want)
  - url: http://localhost:9070/api/v1
    description: Development/test server
  # index 1
  - url: https://api-demo.managemc.com/api/v1
    description: Demo server
  # index 2
  - url: https://api.managemc.com/api/v1
    description: Production server
```

See our [environments guide](../docs/ENVIRONMENTS.md) for help picking a server.

## Token Refreshing

All tokens added to request headers in ManageMC are [JWTs](https://jwt.io/introduction) with an `exp` field, which defines the time (in epoch seconds, not millis) at which the token will expire. There is no grace period. Browse through our authentication API in the OpenAPI editor to find the appropriate refresh endpoints.

## Thread Safety

We haven't explored this concept in detail for Ruby, but encourage you to investigate the following potential problems we identified when working with the Java client:

- The client may not have good support for making concurrent requests, i.e. it is possible that each client instance can only make one request at a time.
- Even if you create multiple client instances in order to combat the above problem, it is possible that the clients will still share the same underlying configuration and/or HTTP client, i.e. if you change the auth token of one instance, this will also change the auth token on all the other instances. This can cause problems in concurrent environments with multiple users.

In [Java](../libs/api-wrapper/src/main/java/com/managemc/api/wrapper/ClientProvider.java), we work around these problems by using `ThreadLocal` to create a new client per thread per authenticated user.
