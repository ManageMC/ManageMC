package com.managemc.spigot.util.chat.formatter;

import com.managemc.spigot.state.ServerGroupsData;
import org.openapitools.client.model.BanOrMute;

public class MuteSuccessFormatter extends BanOrMuteSuccessFormatter {

  public MuteSuccessFormatter(BanOrMute formattable, ServerGroupsData serverGroups) {
    super(formattable, serverGroups);
  }

  @Override
  String getPastTenseVerb() {
    return "muted";
  }
}
