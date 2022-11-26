package com.managemc.spigot.command.util;

import com.managemc.plugins.util.RegexConstants;
import com.managemc.spigot.command.util.punishments.PlayerUuidResolver;
import com.managemc.spigot.command.util.punishments.PunishmentDurations;
import com.managemc.spigot.command.util.punishments.PunishmentFlags;
import com.managemc.spigot.command.util.punishments.model.ResolvedPlayer;
import com.managemc.spigot.command.util.validation.IpV4Validator;
import com.managemc.spigot.command.util.validation.RangeIpValidator;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandAssertions {

  public static final String WRONG_NUM_ARGS = "Wrong number of arguments";
  public static final String NO_SHADOW_PUNISHMENTS_MSG = "You don't have permission to issue shadow punishments";
  public static final String NOT_FOUND_MSG = "Player does not exist. Perhaps they changed their username?";
  public static final String PUNISHMENT_TOO_LONG_MSG = "You don't have permission to issue punishments for that long";
  public static final String NO_PERMANENT_PUNISHMENTS_MSG = "You don't have permission to issue permanent punishments";
  public static final String MOJANG_RATE_LIMIT_MSG = "Could not find player due to Mojang's rate limit";
  public static final String UUID_RESOLUTION_UNEXPECTED_MSG = "Player UUID resolution failed for unexpected reasons";
  public static final String ONLY_PLAYERS_MESSAGE = "Only players can execute this command";
  private static final IpV4Validator IP_V4_VALIDATOR = new IpV4Validator();
  private static final RangeIpValidator RANGE_IP_VALIDATOR = new RangeIpValidator();

  public static void assertTrue(boolean expression, String errorMessage, boolean syntactic) {
    if (!expression) {
      throw new CommandValidationException(errorMessage, syntactic);
    }
  }

  public static void assertArgsLength(String[] args, int length) throws CommandValidationException {
    assertTrue(args.length == length, WRONG_NUM_ARGS, true);
  }

  public static void assertArgsLengthAtLeast(String[] args, int atLeast) {
    assertTrue(args.length >= atLeast, WRONG_NUM_ARGS, true);
  }

  public static String assertValidUsername(String username) {
    assertTrue(username.matches(RegexConstants.USERNAME_REGEX), "Invalid username " + username, true);
    return username;
  }

  public static String assertValidIpAddress(String ipAddress) {
    assertTrue(IP_V4_VALIDATOR.isValid(ipAddress), "Invalid IPv4 address " + ipAddress, true);
    return ipAddress;
  }

  public static String assertValidIpOrIpRange(String ipAddressOrRange) {
    if (ipAddressOrRange.contains("-")) {
      assertTrue(RANGE_IP_VALIDATOR.isValid(ipAddressOrRange), "Invalid IP address range " + ipAddressOrRange, true);
      return ipAddressOrRange;
    }
    return assertValidIpAddress(ipAddressOrRange);
  }

  public static Player assertPlayerSender(CommandSender sender) {
    if (sender instanceof Player) {
      return (Player) sender;
    }
    throw new CommandValidationException(ONLY_PLAYERS_MESSAGE, false);
  }

  public static void checkPermissions(PermissibleAction action, CommandSender sender, String errorMessage) {
    if (!action.isAllowed(sender)) {
      throw new CommandValidationException(errorMessage, false);
    }
  }

  public static void checkPermissionsForPunishmentFlags(PunishmentFlags flags, CommandSender sender) {
    if (flags.isShadow()) {
      checkPermissions(PermissibleAction.SHADOW_PUNISHMENTS, sender, NO_SHADOW_PUNISHMENTS_MSG);
    }

    if (flags.getDuration() == null || flags.getDuration() > PunishmentDurations.LONG_LENGTH) {
      String errorMessage = flags.getDuration() == null ? NO_PERMANENT_PUNISHMENTS_MSG : PUNISHMENT_TOO_LONG_MSG;
      checkPermissions(PermissibleAction.PERMANENT_PUNISHMENTS, sender, errorMessage);
    } else if (flags.getDuration() > PunishmentDurations.MEDIUM_LENGTH) {
      checkPermissions(PermissibleAction.LONG_PUNISHMENTS, sender, PUNISHMENT_TOO_LONG_MSG);
    } else if (flags.getDuration() > PunishmentDurations.SHORT_LENGTH) {
      checkPermissions(PermissibleAction.MEDIUM_PUNISHMENTS, sender, PUNISHMENT_TOO_LONG_MSG);
    }
  }

  public static UUID usernameToUuidBlocking(PlayerUuidResolver resolver, String username) {
    ResolvedPlayer player = resolver.resolvePlayerPotentiallyBlocking(username);

    switch (player.getStatusCode()) {
      case ONLINE_RECENTLY:
      case HTTP_OK:
        return player.getUuid();
      case HTTP_NOT_FOUND:
        throw new CommandValidationException(NOT_FOUND_MSG, false);
      case HTTP_RATE_LIMIT_EXCEEDED:
        throw new CommandValidationException(MOJANG_RATE_LIMIT_MSG, false);
      case HTTP_UNEXPECTED:
      case CLIENT_EXCEPTION:
        throw new RuntimeException(UUID_RESOLUTION_UNEXPECTED_MSG);
      default:
        throw new RuntimeException("Unexpected status code " + player.getStatusCode());
    }
  }
}
