package com.managemc.spigot.util.chat.formatter;

import com.managemc.spigot.state.model.MuteData;
import com.managemc.spigot.util.chat.PluginMessageFormatter;
import lombok.NonNull;

import java.time.Instant;

public class MuteDataFormatter extends ChatFormatterBase<MuteData> {

  public static final String MUTED_HEADER = "You have been muted on this server";

  public MuteDataFormatter(MuteData mute) {
    super(mute);
  }

  @Override
  @NonNull
  public String format() {
    return new PluginMessageFormatter()
        .header(MUTED_HEADER)
        .indent()
        .keyValueData("reason", obj.getReason())
        .keyValueData("expires", obj.getExpiresAt() == null ? null : Instant.ofEpochMilli(obj.getExpiresAt()))
        .toString();
  }
}
