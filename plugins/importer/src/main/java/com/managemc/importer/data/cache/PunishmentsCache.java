package com.managemc.importer.data.cache;

import org.openapitools.client.model.ImportableBanOrMute;
import org.openapitools.client.model.ImportableIpBan;
import org.openapitools.client.model.ImportableWarning;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PunishmentsCache {

  private final Queue<ImportableBanOrMute> bans = new ConcurrentLinkedQueue<>();
  private final Queue<ImportableBanOrMute> mutes = new ConcurrentLinkedQueue<>();
  private final Queue<ImportableWarning> warnings = new ConcurrentLinkedQueue<>();
  private final Queue<ImportableIpBan> ipBans = new ConcurrentLinkedQueue<>();

  void addBan(ImportableBanOrMute ban) {
    bans.add(ban);
  }

  void addMute(ImportableBanOrMute mute) {
    mutes.add(mute);
  }

  void addWarning(ImportableWarning warning) {
    warnings.add(warning);
  }

  void addIpBan(ImportableIpBan ipBan) {
    ipBans.add(ipBan);
  }

  public List<ImportableBanOrMute> allBans() {
    return new ArrayList<>(bans);
  }

  public List<ImportableBanOrMute> allMutes() {
    return new ArrayList<>(mutes);
  }

  public List<ImportableWarning> allWarnings() {
    return new ArrayList<>(warnings);
  }

  public List<ImportableIpBan> allIpBans() {
    return new ArrayList<>(ipBans);
  }

  public int size() {
    return bans.size() + mutes.size() + warnings.size() + ipBans.size();
  }
}
