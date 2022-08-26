package com.managemc.spigot.config.model;

import com.managemc.plugins.bukkit.BukkitWrapper;
import com.managemc.plugins.logging.BukkitLogging;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsManager {

  private static final String NO_PERMS_FOUND = "No saved permissions found for player: %s";
  private static final String NO_PERMS_ATT_FOUND = "No permission attachment found for player: %s";

  private final BukkitLogging logging;
  private final BukkitWrapper bukkitWrapper;
  private final Map<UUID, List<String>> intendedPermissions = new ConcurrentHashMap<>();
  private final Map<UUID, PermissionAttachment> permissions = new ConcurrentHashMap<>();

  public PermissionsManager(BukkitLogging logging, BukkitWrapper bukkitWrapper) {
    this.logging = logging;
    this.bukkitWrapper = bukkitWrapper;
  }

  /**
   * Permissions must be assigned to an actual entity (e.g. Player), which is not
   * available in the async pre-login event. But we do have the UUID, so we remember
   * the UUID/permissions of the player who will be logging in soon, then assign those
   * permissions to the player once they have joined as a Permissible entity.
   */
  public void registerIntentToAssignPermissions(UUID uuid, List<String> permissions) {
    intendedPermissions.put(uuid, permissions);
  }

  /**
   * ManageMC never tries to interfere with permissions assigned by other plugins. This
   * is why we do not attempt to "revoke" permissions that are revoked/excluded from
   * roles and specific players on our website. Our API simply tells us which permissions
   * a player SHOULD have, and then we assign them all here.
   */
  public void assignPermissions(Player player) {
    PermissionAttachment attachment = new PermissionAttachment(bukkitWrapper.getPlugin(), player);

    if (intendedPermissions.containsKey(player.getUniqueId())) {
      intendedPermissions.get(player.getUniqueId()).forEach(p -> attachment.setPermission(p, true));
      intendedPermissions.remove(player.getUniqueId());
      permissions.put(player.getUniqueId(), attachment);
    } else {
      String message = String.format(NO_PERMS_FOUND, player.getName());
      logging.logWarning(message);
    }
  }

  public void clearPermissions(Player player) {
    if (permissions.containsKey(player.getUniqueId())) {
      permissions.get(player.getUniqueId()).remove();
    } else {
      String message = String.format(NO_PERMS_ATT_FOUND, player.getName());
      logging.logWarning(message);
    }
  }
}
