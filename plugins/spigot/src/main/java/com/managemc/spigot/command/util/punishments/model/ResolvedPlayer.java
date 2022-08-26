package com.managemc.spigot.command.util.punishments.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Builder
public class ResolvedPlayer {
  @Getter
  private final UUID uuid;
  @Getter
  @NonNull
  private final Status statusCode;

  public enum Status {
    ONLINE_RECENTLY, HTTP_OK, HTTP_NOT_FOUND, HTTP_RATE_LIMIT_EXCEEDED, HTTP_UNEXPECTED, CLIENT_EXCEPTION
  }
}
