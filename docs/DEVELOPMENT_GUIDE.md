# Development Guide

Anyone can contribute to our public code base here. This guide should be sufficient to get you started; if it is not, please open a pull request and/or request improvements.

## Preliminary Steps

### Download JDK 8

[Download](https://www.oracle.com/java/technologies/downloads) the Java 8 development kit (JDK) and [add it](https://www.baeldung.com/java-home-on-windows-7-8-10-mac-os-x-linux) to your environment. You should see output simiar to the following (the version should start with `1.8`):

```txt
➜  java -version

java version "1.8.0_77"
Java(TM) SE Runtime Environment (build 1.8.0_77-b03)
Java HotSpot(TM) 64-Bit Server VM (build 25.77-b03, mixed mode)
```

### Authenticate with GitHub Packages

Our libraries are currently published to [GitHub Packages](https://github.com/features/packages). Maven will try to download these packages periodically as you work with our code base, but since GitHub Packages does not support unauthorized downloads, even for public resources, we need to prove to GitHub that you are a real GitHub user. We do this by creating a personal access token and adding it to your Maven settings.

1. Create a GitHub account if you don't already have one
2. Go [here](https://github.com/settings/tokens) and generate a new token. Set the expiration to `No Expiration` and select only the `read:packages` box. After you generate the token, keep the page open so that you can copy the token later.
3. Install Maven using your preferred package manager (e.g. `brew install maven`). If you can use the `mvn` command in your terminal, it is most likely already installed.
4. Locate your Maven user settings file:

   ```sh
   ➜  mvn -X clean | grep "settings"

   [DEBUG]   Imported: org.apache.maven.settings < plexus.core
   [DEBUG] Reading global settings from /usr/local/Cellar/maven/3.8.1/libexec/conf/settings.xml
   [DEBUG] Reading user settings from /Users/jacobcrofts/.m2/settings.xml
   ```

   The above output indicates that the user settings file is found at `/Users/jacobcrofts/.m2/settings.xml`.
5. Configure your user settings file, adding a `server` block like this:

   ```xml
   <settings>
      <!-- ... -->
      <servers>
         <!-- ... -->
         <server>
            <id>github</id>
            <configuration>
               <httpHeaders>
                  <property>
                     <name>Authorization</name>
                     <!-- copy your GitHub token and paste it here after "Bearer" -->
                     <value>Bearer ghp_therestofyourtoken</value>
                  </property>
               </httpHeaders>
            </configuration>
         </server>
      </servers>
   </settings>
   ```

## Next Steps

Once you have reached this point, you may begin developing. We have a few other optional guides that may be useful, depending on what you are trying to do:

- architecture overview
- using our API
- testing your changes
