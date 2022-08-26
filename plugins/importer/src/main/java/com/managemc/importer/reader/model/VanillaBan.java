package com.managemc.importer.reader.model;

import lombok.Builder;
import lombok.Getter;

// used by Gson library
@Builder
public class VanillaBan {

  @Getter
  private final String uuid;
  @Getter
  private final String name;
  @Getter
  private final String created;
  @Getter
  private final String source;
  @Getter
  private final String expires;
  @Getter
  private final String reason;
}
