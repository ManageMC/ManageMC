package com.managemc.spigot.util.chat.formatter;

import lombok.NonNull;

public abstract class ChatFormatterBase<T> {

  protected T obj;

  public ChatFormatterBase(T obj) {
    this.obj = obj;
  }

  @NonNull
  public abstract String format();
}
