package com.managemc.importer.reader;

import com.managemc.importer.reader.model.MaxBansPlusPunishmentType;
import com.managemc.importer.reader.source.MaxBansPlusDb;
import org.maxgamer.maxbans.orm.Restriction;

import java.util.HashMap;
import java.util.Map;

public class MaxBansPlusReader {

  private final MaxBansPlusDb db;

  public MaxBansPlusReader(MaxBansPlusDb db) {
    this.db = db;
  }

  public Map<Restriction, MaxBansPlusPunishmentType> read() {
    Map<Restriction, MaxBansPlusPunishmentType> punishments = new HashMap<>();

    db.getAllBans().forEach(ban -> {
      MaxBansPlusPunishmentType type = isIpOriented(ban)
          ? MaxBansPlusPunishmentType.IP_BAN
          : MaxBansPlusPunishmentType.BAN;
      punishments.put(ban, type);
    });

    db.getAllMutes().stream()
        .filter(punishment -> !isIpOriented(punishment))
        .forEach(mute -> punishments.put(mute, MaxBansPlusPunishmentType.MUTE));

    db.getAllWarnings().forEach(warning -> punishments.put(warning, MaxBansPlusPunishmentType.WARNING));

    return punishments;
  }

  private boolean isIpOriented(Restriction punishment) {
    return punishment.getTenant().getName().contains(".");
  }
}
