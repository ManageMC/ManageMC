package com.managemc.spigot.command.util;

import com.managemc.spigot.util.permissions.PermissibleAction;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Builder
class CommandNode {
  @Getter
  private final Collection<CommandNode> children;
  @Getter
  private final String keyword;
  @Getter
  private final CommandProcessorBase handler;
  @Getter
  private final PermissibleAction action;

  public boolean matchesFully(String word) {
    return word.equals(keyword);
  }

  public boolean matchesPartially(String word) {
    return keyword.startsWith(word);
  }

  public Collection<CommandNode> getPermissibleChildren(CommandSender sender) {
    if (sender instanceof Player) {
      return children.stream()
          .filter(child -> Optional.ofNullable(child.action)
              .map(action -> action.isAllowed(sender))
              .orElse(true))
          .collect(Collectors.toList());
    }
    return children;
  }
}
