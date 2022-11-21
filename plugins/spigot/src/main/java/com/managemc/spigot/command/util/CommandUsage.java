package com.managemc.spigot.command.util;

public class CommandUsage {
  public static final String BANS_CREATE = "/mmc bans create <username> [reason] [details] [flags]";
  public static final String PLAYERS_CREATE = "/mmc players create <username>";
  public static final String PUNISHMENTS_GET = "/mmc punishments get <username>";
  public static final String IPBANS_CREATE = "/mmc ipbans create <ip or username> [reason] [details] [flags]";
  public static final String MUTES_CREATE = "/mmc mutes create <username> [reason] [details] [flags]";
  public static final String PUNISHMENTS_PARDON = "/mmc punishments pardon <ID...> [details]";
  public static final String WARNINGS_CREATE = "/mmc warnings create <username> [reason (can be multiple words)]";
  public static final String WATCHLIST_SHOW = "/mmc watchlist show";
  public static final String WATCHLIST_ADD = "/mmc watchlist add <username>";
  public static final String WATCHLIST_REMOVE = "/mmc watchlist remove <username>";
}
