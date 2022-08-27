# Local Test Server

This is a simple Spigot server for testing changes to ManageMC plugins. It comes with setup scripts to save time, but you are encouraged to browse Spigot's docs if you are unfamiliar with them and/or curious:

- [installation guide](https://www.spigotmc.org/wiki/spigot-installation/)
- [configuration guide](https://www.spigotmc.org/wiki/spigot-configuration/)

Note: this guide is designed for use on macOS and Linux. It may also work on Windows 10+. If you encounter problems, contributions are welcome!

## Setup

1. Follow the instructions in our [development guide](../plugins/DEVELOPMENT_GUIDE.md) to prepare your local environment.

2. Run the BuildTools script with the version of Spigot that you wish to test the plugins on:

   ```txt
   ➜  bin/buildtools 1.19.2
   ```

3. Now start your server:

   ```txt
   bin/start_spigot
   ```

4. You will be asked to accept Minecraft's license agreement the first time you try to run the server. Follow the instructions on the command prompt.

5. Run the server a second time, wait for it to finish loading, and now you will be able to join the server with your Minecraft client at `localhost:25565`.

## Testing Plugins

There is a script that will automatically build a given plugin, along with any changes you have made to it locally, and copy it into the `spigot/plugins/` directory:

```sh
bin/local_plugin
```

After running this command, simply start your server again or use the `/reload` command if your server was already running. There is no need to restart every time.

### API Keys

Each plugin needs API keys in order to communicate with ManageMC. The first time Spigot tries to load each plugin, the plugin will generate a configuration file and alert you about it in the server logs. Follow the instructions in that file.
