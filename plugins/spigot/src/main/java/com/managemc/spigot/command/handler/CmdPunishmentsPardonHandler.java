package com.managemc.spigot.command.handler;

import com.managemc.api.ApiException;
import com.managemc.spigot.command.handler.base.CommandHandlerAsync;
import com.managemc.spigot.command.util.CommandAssertions;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.permissions.PermissibleAction;
import org.bukkit.command.CommandSender;
import org.openapitools.client.model.PunishmentIdAndType;

import java.util.*;
import java.util.stream.Collectors;

public class CmdPunishmentsPardonHandler extends CommandHandlerAsync {

  public static final String ID_REGEX;
  public static final String BAD_ID_MESSAGE = "One or more invalid punishment IDs detected (correct example: ban-123)";
  public static final String NO_BAN_MSG = "You cannot pardon bans";
  public static final String NO_MUTE_MSG = "You cannot pardon mutes";
  public static final String NO_WARNING_MSG = "You cannot pardon warnings";
  public static final String NO_IP_BAN_MSG = "You cannot pardon IP bans";

  static {
    List<String> typeOptions = Arrays.stream(PunishmentIdAndType.TypeEnum.values()).map(Enum::name).collect(Collectors.toList());
    ID_REGEX = "^(" + String.join("|", typeOptions) + ")-\\d+$";
  }

  private final SpigotPluginConfig config;

  public CmdPunishmentsPardonHandler(CommandSender sender, ProcessedCommandArguments args, SpigotPluginConfig config) {
    super(sender, args);
    this.config = config;
  }

  private final Collection<String> punishmentIds = new HashSet<>();
  private String details;

  /**
   * Special note here: the "pardon-other" permission (com.managemc.staff.punishments.pardon)
   * allows a user to pardon punishments issued by other players. All users can pardon
   * their own punishments, provided they still have permission to issue the given
   * punishment type, but since this plugin has no way of knowing which player issued a
   * punishment without making API requests, we do not check the "pardon-other" permission
   * client-side. If the server throws a 403 when we try to pardon, then we display a permission
   * error to the command sender asynchronously. We do this for every player-initiated API
   * request, because it's always possible for permissions to be out of sync.
   */
  @Override
  public void preProcessCommand() {
    CommandAssertions.assertArgsLengthAtLeast(args.getArgs(), 1);
    List<String> allDetails = new ArrayList<>();
    for (String arg : args.getArgs()) {
      if (allDetails.size() > 0) {
        allDetails.add(arg);
      } else if (arg.toUpperCase().matches(ID_REGEX)) {
        punishmentIds.add(arg);
      } else {
        allDetails.add(arg);
      }
    }

    CommandAssertions.assertTrue(punishmentIds.size() > 0, BAD_ID_MESSAGE, true);

    Set<PunishmentIdAndType.TypeEnum> allTypesRequested = new HashSet<>();
    punishmentIds.forEach(id -> {
      String type = id.split("-")[0].toUpperCase();
      allTypesRequested.add(PunishmentIdAndType.TypeEnum.fromValue(type));
    });

    allTypesRequested.forEach(type -> {
      switch (type) {
        case BAN:
          CommandAssertions.checkPermissions(PermissibleAction.BAN_PLAYER, sender, NO_BAN_MSG);
          break;
        case MUTE:
          CommandAssertions.checkPermissions(PermissibleAction.MUTE_PLAYER, sender, NO_MUTE_MSG);
          break;
        case WARNING:
          CommandAssertions.checkPermissions(PermissibleAction.WARN_PLAYER, sender, NO_WARNING_MSG);
          break;
        case IP_BAN:
        case RANGE_IP_BAN:
          CommandAssertions.checkPermissions(PermissibleAction.IP_BAN_PLAYER, sender, NO_IP_BAN_MSG);
          break;
      }
    });

    details = allDetails.size() == 0 ? null : String.join(" ", allDetails);
  }

  @Override
  public void processCommandAsync() throws ApiException {
    config.getPardonService().issuePardon(punishmentIds, details, sender);
  }
}
