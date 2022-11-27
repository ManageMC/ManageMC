package com.managemc.spigot.command.util.punishments;

import com.managemc.spigot.command.util.CommandFlag;
import com.managemc.spigot.command.util.MultiWordArguments;
import com.managemc.spigot.command.util.ProcessedCommandArguments;
import com.managemc.spigot.config.model.LocalConfig;
import lombok.Getter;
import org.openapitools.client.model.CreateBanOrMuteInput;
import org.openapitools.client.model.CreateIpBanInput;
import org.openapitools.client.model.PunishmentVisibility;

import java.util.*;

public class PunishmentFlags {

  public static final String BROADCAST_FLAG = "broadcast";
  public static final String PRIVATE_FLAG = "private";
  public static final String SHADOW_FLAG = "shadow";
  public static final String LOCAL_FLAG = "local";
  public static final String DURATION_FLAG = "duration";

  public static final Collection<CommandFlag> FLAGS = new HashSet<>(
      Arrays.asList(
          CommandFlag.argumentFlag(DURATION_FLAG),
          CommandFlag.defaultFlag(BROADCAST_FLAG),
          CommandFlag.defaultFlag(PRIVATE_FLAG),
          CommandFlag.defaultFlag(SHADOW_FLAG),
          CommandFlag.defaultFlag(LOCAL_FLAG)
      )
  );
  private final ProcessedCommandArguments args;
  @Getter
  private Long duration;

  public PunishmentFlags(ProcessedCommandArguments args) {
    this.args = args;
    if (args.hasFlag(DURATION_FLAG)) {
      String durationArgument = args.getFlag(DURATION_FLAG);
      this.duration = new DurationStringParser().parse(durationArgument);
    }
  }

  public boolean isBroadcast() {
    return args.hasFlag(BROADCAST_FLAG);
  }

  public boolean isShadow() {
    return args.hasFlag(SHADOW_FLAG);
  }

  public boolean isPrivate() {
    return args.hasFlag(PRIVATE_FLAG);
  }

  public boolean isLocal() {
    return args.hasFlag(LOCAL_FLAG);
  }

  public CreateBanOrMuteInput toBanOrMuteInput(LocalConfig localConfig) {
    List<Long> localScope = Collections
        .singletonList(localConfig.getDynamicComponents().getServerMetadata().getServerGroupId());

    return new CreateBanOrMuteInput()
        .visibility(PunishmentVisibility.fromValue(getVisibility()))
        .scope(isLocal() ? localScope : null)
        .reason(getReason())
        .details(getDetails())
        .durationMillis(duration);
  }

  public CreateIpBanInput toIpBanInput(LocalConfig localConfig) {
    List<Long> localScope = Collections
        .singletonList(localConfig.getDynamicComponents().getServerMetadata().getServerGroupId());

    return new CreateIpBanInput()
        .visibility(PunishmentVisibility.fromValue(getVisibility()))
        .scope(isLocal() ? localScope : null)
        .reason(getReason())
        .details(getDetails())
        .durationMillis(duration);
  }

  public String getReason() {
    return args.get(1);
  }

  private String getDetails() {
    return MultiWordArguments.startingAtIndex(args.getArgs(), 2);
  }

  private String getVisibility() {
    if (isShadow()) {
      return PunishmentVisibility.SHADOW.toString();
    }

    if (isPrivate()) {
      return PunishmentVisibility.PRIVATE.toString();
    }

    return PunishmentVisibility.PUBLIC.toString();
  }
}
