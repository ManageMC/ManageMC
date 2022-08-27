package com.managemc.spigot.util.chat.formatter;

import com.managemc.spigot.state.ServerGroupsData;
import com.managemc.spigot.util.chat.PluginMessageFormatter;
import lombok.NonNull;
import org.openapitools.client.model.IpBan;
import org.openapitools.client.model.ServerGroupAbridged;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class IpBanSuccessFormatter extends ChatFormatterBase<IpBan> {

  private final ServerGroupsData serverGroups;

  public IpBanSuccessFormatter(IpBan obj, ServerGroupsData serverGroups) {
    super(obj);
    this.serverGroups = serverGroups;
  }

  @Override
  public @NonNull String format() {
    List<String> groupNames = obj.getScope().stream()
        .map(serverGroups::getServerGroup)
        .map(ServerGroupAbridged::getLabel)
        .collect(Collectors.toList());

    Long expiresAtMillis = obj.getDuration() == null ? null : obj.getDuration() + obj.getIssuedAt();

    return new PluginMessageFormatter()
        .header("Successfully IP-banned " + obj.getIpAddressOrRange())
        .indent()
        .keyValueDataIdRow(obj.getType() + "-" + obj.getId())
        .keyValueData("reason", obj.getReason())
        .keyValueData("issued by", obj.getIssuer() == null ? "Automatic/Console" : obj.getIssuer().getUsername())
        .keyValueData("scope", obj.getScope().size() == 0 ? "" : String.join(", ", groupNames))
        .keyValueData("created", Instant.ofEpochMilli(obj.getIssuedAt()))
        .keyValueData("expires", expiresAtMillis == null ? null : Instant.ofEpochMilli(expiresAtMillis))
        .toString();
  }
}