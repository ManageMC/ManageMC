package com.managemc.importer.reader.source;

import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.SQLQuery;

import java.util.List;

public class AdvancedBanDbImpl implements AdvancedBanDb {

  @Override
  public List<Punishment> getPunishments() {
    return PunishmentManager.get().getPunishments(SQLQuery.SELECT_ALL_PUNISHMENTS);
  }

  @Override
  public List<Punishment> getPunishmentHistory() {
    return PunishmentManager.get().getPunishments(SQLQuery.SELECT_ALL_PUNISHMENTS_HISTORY);
  }
}
