package com.managemc.spigot.util.chat.formatter;

import com.managemc.spigot.util.chat.PluginMessageFormatter;
import lombok.NonNull;
import org.openapitools.client.model.Warning;

import java.time.Instant;

public class WarningFormatter extends ChatFormatterBase<Warning> {

  public WarningFormatter(Warning obj) {
    super(obj);
  }

  @Override
  @NonNull
  public String format() {
    return new PluginMessageFormatter()
        .line("-")
        .indent()
        .keyValueDataIdRow(obj.getType() + "-" + obj.getId())
        .keyValueData("reason", obj.getReason())
        .keyValueData("issued by", obj.getIssuer() == null ? "Automatic/Console" : obj.getIssuer().getUsername())
        .keyValueData("created", Instant.ofEpochMilli(obj.getIssuedAt()))
        .toString();
  }
}
