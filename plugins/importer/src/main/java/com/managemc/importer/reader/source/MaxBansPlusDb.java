package com.managemc.importer.reader.source;

import org.maxgamer.maxbans.orm.Ban;
import org.maxgamer.maxbans.orm.Mute;
import org.maxgamer.maxbans.orm.Warning;

import java.util.List;

public interface MaxBansPlusDb {

  List<Ban> getAllBans();

  List<Mute> getAllMutes();

  List<Warning> getAllWarnings();
}
