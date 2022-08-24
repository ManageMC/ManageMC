package com.managemc.spigot.state;

import com.managemc.spigot.util.oneof.PlayerPunishmentType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.openapitools.client.model.BanOrMute;
import org.openapitools.client.model.IpBan;
import org.openapitools.client.model.PlayerPunishment;
import org.openapitools.client.model.Warning;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Used for really, really smart tab completion of the pardon command. Use null instead of a UUID
 * for ConsoleCommandSender.
 */
public class PardonablePunishmentData {

  // assign a random UUID to console command senders
  private static final UUID CONSOLE_UUID = UUID.randomUUID();

  private final Map<UUID, Set<String>> punishmentsKnownToEachPlayer = new ConcurrentHashMap<>();

  public void addPunishment(CommandSender sender, String externalId) {
    addPunishment(determineUuid(sender), externalId);
  }

  public void addPunishment(UUID uuid, String externalId) {
    getKnownPunishments(uuid).add(externalId.toLowerCase());
  }

  public void addPunishment(CommandSender sender, BanOrMute banOrMute) {
    addPunishment(sender, banOrMute.getExternalId());
  }

  public void addPunishment(CommandSender sender, Warning warning) {
    addPunishment(sender, warning.getExternalId());
  }

  public void addPunishment(CommandSender sender, IpBan ipBan) {
    addPunishment(sender, ipBan.getExternalId());
  }

  public void addPunishments(CommandSender sender, Collection<PlayerPunishment> punishments) {
    Collection<String> punishmentIds = punishments.stream()
        .map(this::getExternalId)
        .map(String::toLowerCase)
        .collect(Collectors.toList());

    getKnownPunishments(determineUuid(sender)).addAll(punishmentIds);
  }

  private String getExternalId(PlayerPunishment punishment) {
    switch (PlayerPunishmentType.of(punishment)) {
      case BAN_OR_MUTE:
        return punishment.getBanOrMute().getExternalId();
      case WARNING:
        return punishment.getWarning().getExternalId();
      default:
        throw new RuntimeException("Unexpected PlayerPunishmentType " + PlayerPunishmentType.of(punishment));
    }
  }

  public List<String> getPunishmentIdsKnownToSender(CommandSender sender) {
    return getPunishmentIdsKnownToSender(determineUuid(sender));
  }

  public List<String> getPunishmentIdsKnownToSender(UUID uuid) {
    Set<String> knownPunishments = punishmentsKnownToEachPlayer.get(uuid);
    if (knownPunishments == null) {
      return new ArrayList<>();
    }
    return knownPunishments.stream().sorted().collect(Collectors.toList());
  }

  public void clearPardonedPunishments(Collection<String> punishmentIds) {
    Collection<String> lowerCaseIds = punishmentIds.stream().map(String::toLowerCase).collect(Collectors.toList());
    punishmentsKnownToEachPlayer.values()
        .forEach(knownPunishments -> lowerCaseIds.forEach(knownPunishments::remove));
  }

  private Set<String> getKnownPunishments(UUID uuid) {
    return punishmentsKnownToEachPlayer.computeIfAbsent(uuid, key -> new HashSet<>());
  }

  private UUID determineUuid(CommandSender sender) {
    if (sender instanceof Player) {
      return ((Player) sender).getUniqueId();
    }
    return CONSOLE_UUID;
  }
}
