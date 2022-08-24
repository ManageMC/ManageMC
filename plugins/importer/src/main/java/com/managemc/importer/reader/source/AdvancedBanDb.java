package com.managemc.importer.reader.source;

import me.leoko.advancedban.utils.Punishment;

import java.util.List;

public interface AdvancedBanDb {

  List<Punishment> getPunishments();

  List<Punishment> getPunishmentHistory();
}
