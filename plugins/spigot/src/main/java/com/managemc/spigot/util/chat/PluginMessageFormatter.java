package com.managemc.spigot.util.chat;

import com.managemc.spigot.util.TimeHelper;
import org.bukkit.ChatColor;

import java.time.Instant;

public class PluginMessageFormatter {

  private static final String AQUA = String.valueOf(ChatColor.AQUA);
  private static final String BLUE = String.valueOf(ChatColor.BLUE);
  private static final String DARK_PURPLE = String.valueOf(ChatColor.DARK_PURPLE);
  private static final String RED = String.valueOf(ChatColor.RED);
  private static final String GREEN = String.valueOf(ChatColor.GREEN);
  private static final String GOLD = String.valueOf(ChatColor.GOLD);
  private static final String YELLOW = String.valueOf(ChatColor.YELLOW);
  private static final String DARK_GREEN = String.valueOf(ChatColor.DARK_GREEN);
  private static final String LIGHT_PURPLE = String.valueOf(ChatColor.LIGHT_PURPLE);

  private static final String LABEL = LIGHT_PURPLE + "[" + AQUA + "MMC" + LIGHT_PURPLE + "]";
  private static final String NULL = ChatColor.GRAY + "null";

  private final StringBuilder builder = new StringBuilder();
  private int indent = 0;

  public PluginMessageFormatter header(String header) {
    builder.append(LABEL).append(" ").append(AQUA).append(ChatColor.stripColor(header));
    applyNewline();
    return this;
  }

  public PluginMessageFormatter indent() {
    indent++;
    return this;
  }

  public PluginMessageFormatter unIndent() {
    indent--;
    return this;
  }

  public PluginMessageFormatter line(String text) {
    applyIndent();
    builder.append(AQUA).append(ChatColor.stripColor(text));
    applyNewline();
    return this;
  }

  public PluginMessageFormatter keyValueDataIdRow(Object value) {
    return keyValueData("ID", value, DARK_PURPLE);
  }

  public PluginMessageFormatter keyValueData(String key, String value) {
    return keyValueData(key, value, BLUE);
  }

  public PluginMessageFormatter keyValueData(String key, Number value) {
    return keyValueData(key, value, DARK_GREEN);
  }

  public PluginMessageFormatter keyValueData(String key, Boolean value) {
    String color = Boolean.valueOf(true).equals(value) ? GREEN : RED;
    return keyValueData(key, value, color);
  }

  public <E extends Enum<E>> PluginMessageFormatter keyValueData(String key, Enum<E> value) {
    return keyValueData(key, value, GOLD);
  }

  public PluginMessageFormatter keyValueData(String key, Instant timestamp) {
    String time = timestamp == null ? null : TimeHelper.relativeTime(timestamp.toEpochMilli());
    return keyValueData(key, time, YELLOW);
  }

  public PluginMessageFormatter append(String text) {
    builder.append(text);
    return this;
  }

  @Override
  public String toString() {
    return builder.toString().trim();
  }

  private PluginMessageFormatter keyValueData(String key, Object value, String valueColor) {
    applyIndent();
    String val = value == null ? NULL : valueColor + ChatColor.stripColor(String.valueOf(value));
    builder.append(AQUA).append(key).append(": ").append(val);
    applyNewline();
    return this;
  }

  private void applyNewline() {
    builder.append("\n");
  }

  private void applyIndent() {
    for (int i = 0; i < indent; i++) {
      builder.append("  ");
    }
  }
}
