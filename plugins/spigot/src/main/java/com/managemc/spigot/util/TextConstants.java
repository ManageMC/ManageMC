package com.managemc.spigot.util;

import org.bukkit.ChatColor;

public class TextConstants {
  public static class Commands {
    public static class Punishment {
      public static final String PUNISHMENT_404 = ChatColor.RED + "%s is a real player but has never been seen before by ManageMC. You can use the /create-player command to add them to our database.";
    }

    public static final String BROADCAST_FLAG_IGNORED_MESSAGE = ChatColor.YELLOW + "The --broadcast flag is ignored for shadow and private punishments, as these are always hidden from other players";
    public static final String PRIVATE_FLAG_REDUNDANT_MESSAGE = ChatColor.YELLOW + "The --private flag is not needed for shadow punishments, as these will always be hidden from the public";
  }

  public static final String INSUFFICIENT_PERMS = ChatColor.RED + "Insufficient permissions";
}
