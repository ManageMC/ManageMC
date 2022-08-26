package com.managemc.spigot.util.chat.formatter;

import com.managemc.spigot.util.chat.PluginMessageFormatter;
import lombok.NonNull;
import org.openapitools.client.model.Warning;

import java.time.Instant;

public class WarningSuccessFormatter extends ChatFormatterBase<Warning> {

  public static final String WARNING_SUCCESS_MESSAGE = "Successfully warned %s";

  public WarningSuccessFormatter(Warning obj) {
    super(obj);
  }

  @Override
  @NonNull
  public String format() {
    return new PluginMessageFormatter()
        .header(String.format(WARNING_SUCCESS_MESSAGE, obj.getOffender().getUsername()))
        .indent()
        .keyValueDataIdRow(obj.getType() + "-" + obj.getId())
        .keyValueData("reason", obj.getReason())
        .keyValueData("issued by", obj.getIssuer() == null ? "Automatic/Console" : obj.getIssuer().getUsername())
        .keyValueData("created", Instant.ofEpochMilli(obj.getIssuedAt()))
        .toString();
  }
}
