package com.managemc.importer.reader;

import java.util.List;

public interface PunishmentReader<T> {

  List<T> read() throws Exception;
}
