package com.managemc.importer.command.base;

import lombok.Builder;
import lombok.Getter;

@Builder
public class CommandBuilder {

  @Getter
  private String name;
  @Getter
  private String description;
  @Getter
  private String usage;
}
