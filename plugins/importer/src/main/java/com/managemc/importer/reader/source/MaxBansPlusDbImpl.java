package com.managemc.importer.reader.source;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.maxgamer.maxbans.config.PluginConfig;
import org.maxgamer.maxbans.exception.ConfigException;
import org.maxgamer.maxbans.orm.*;
import org.maxgamer.maxbans.transaction.TransactionLayer;
import org.maxgamer.maxbans.transaction.Transactor;

import java.util.List;

public class MaxBansPlusDbImpl implements MaxBansPlusDb {

  private final Transactor worker;

  public MaxBansPlusDbImpl() {
    PluginConfig pluginConfig = loadMaxBans();
    Configuration hibernate = HibernateConfigurer.configuration(pluginConfig.getJdbcConfig());
    SessionFactory sessionFactory = hibernate.buildSessionFactory();
    this.worker = new Transactor(sessionFactory);
  }

  @Override
  public List<Ban> getAllBans() {
    return getAll("Ban", Ban.class);
  }

  @Override
  public List<Mute> getAllMutes() {
    return getAll("Mute", Mute.class);
  }

  @Override
  public List<Warning> getAllWarnings() {
    return getAll("Warning", Warning.class);
  }


  private <T extends Restriction> List<T> getAll(String tableName, Class<T> recordClass) {
    try (TransactionLayer tx = worker.transact()) {
      List<T> records = tx.getEntityManager()
          .createQuery("from " + tableName, recordClass)
          .getResultList();

      // loads the lazy-loaded things inside the transaction
      records.forEach(record -> {
        Hibernate.initialize(record.getSource());
        Hibernate.initialize(record.getRevoker());
        Hibernate.initialize(record.getTenant());
      });

      return records;
    }
  }

  private PluginConfig loadMaxBans() {
    Plugin maxBans = Bukkit.getPluginManager().getPlugin("MaxBansPlus");

    if (maxBans == null) {
      throw new RuntimeException("Could not find MaxBansPlus plugin");
    }

    try {
      return new PluginConfig(maxBans.getConfig(), maxBans.getServer());
    } catch (ConfigException e) {
      throw new RuntimeException(e);
    }
  }
}
