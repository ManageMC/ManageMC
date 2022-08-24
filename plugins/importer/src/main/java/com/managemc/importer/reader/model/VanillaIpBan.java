package com.managemc.importer.reader.model;

import lombok.Builder;
import lombok.Getter;

@Builder
public class VanillaIpBan {

  @Getter
  private final String ip;
  @Getter
  private final String created;
  @Getter
  private final String source;
  @Getter
  private final String expires;
  @Getter
  private final String reason;
}
