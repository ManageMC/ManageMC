package com.managemc.importer.reader;

import com.managemc.importer.reader.source.AdvancedBanDb;
import me.leoko.advancedban.utils.Punishment;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AdvancedBanReader {

  private final AdvancedBanDb db;

  public AdvancedBanReader(AdvancedBanDb db) {
    this.db = db;
  }

  /**
   * @return a map of punishment -> whether punishment is pardoned
   */
  public Map<Punishment, Boolean> read() {
    // Map<uuid, Map<startTime, R>
    // used to quickly check whether a punishment is active
    // note that we CANNOT count on the IDs in the punishments and history tables being the same,
    // so indexing by attributes is the only sound way.
    Map<String, Map<Long, Punishment>> indexedActivePunishments = new HashMap<>();

    db.getPunishments().forEach(record -> {
      indexedActivePunishments.putIfAbsent(record.getUuid(), new HashMap<>());
      indexedActivePunishments.get(record.getUuid()).put(record.getStart(), record);
    });

    return db.getPunishmentHistory().stream()
        .collect(Collectors.toMap(p -> p, p -> isPardoned(p, indexedActivePunishments)));
  }

  private boolean isPardoned(Punishment punishment, Map<String, Map<Long, Punishment>> indexedActivePunishments) {
    Map<Long, Punishment> punishmentsByStartTime = indexedActivePunishments.get(punishment.getUuid());

    if (punishmentsByStartTime == null) {
      return true;
    }

    return !punishmentsByStartTime.containsKey(punishment.getStart());
  }
}
