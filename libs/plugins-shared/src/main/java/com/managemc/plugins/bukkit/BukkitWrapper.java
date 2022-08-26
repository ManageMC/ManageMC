package com.managemc.plugins.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * All methods in this class are meant to be stubbed during testing because:
 * - (1) they are all difficult and/or tedious to test in a truly end-to-end way;
 * - (2) they are not valuable to test (we operate under the assumption that "Bukkit works as documented")
 */
public class BukkitWrapper {

  @Getter
  private final JavaPlugin plugin;

  public BukkitWrapper(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void shutdown() {
    plugin.getServer().shutdown();
  }

  public File getDataFolder() {
    return plugin.getDataFolder();
  }

  public InputStream getResource(String fileName) {
    return plugin.getResource(fileName);
  }

  public void registerListener(Listener listener) {
    plugin.getServer().getPluginManager().registerEvents(listener, plugin);
  }

  @Nullable
  public Player getOnlineOrRecentlyOnlinePlayer(String username) {
    return Bukkit.getPlayer(username);
  }

  @Nullable
  public Player getOnlinePlayer(String username) {
    Player player = getOnlineOrRecentlyOnlinePlayer(username);
    return player != null && player.isOnline() ? player : null;
  }

  public Player getOnlinePlayer(UUID uuid) {
    Player player = Bukkit.getPlayer(uuid);
    return player != null && player.isOnline() ? player : null;
  }

  public Set<Player> getOnlinePlayers() {
    return new HashSet<>(Bukkit.getOnlinePlayers());
  }

  public void broadcastMessage(String message) {
    Bukkit.broadcastMessage(message);
  }
}
