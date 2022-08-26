package com.managemc.spigot.command.util;

import com.managemc.spigot.command.processor.*;
import com.managemc.spigot.config.SpigotPluginConfig;
import com.managemc.spigot.util.permissions.PermissibleAction;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public class CommandNodeMap implements TabCompleter {

  static final String NO_ARGS_MSG = "Please supply an action or resource to your command. Valid options are: %s";
  static final String INVALID_WORD = "Invalid keyword \"%s\". Valid option(s) are: %s";
  static final String AMBIGUOUS_WORD = "Ambiguous keyword \"%s\". Matching options are: %s";
  public static final String NO_PERMS_MSG = "You don't have permission to use that command";

  private final Collection<CommandNode> nodes;

  public CommandNodeMap(SpigotPluginConfig config) {
    nodes = new HashSet<CommandNode>() {{
      add(CommandNode.builder()
          .keyword("punishments")
          .action(PermissibleAction.TAB_COMPLETE_MMC_PUNISHMENTS)
          .children(
              new HashSet<CommandNode>() {{
                add(CommandNode.builder().keyword("get")
                    .handler(new CmdPunishmentsGet(config))
                    .action(PermissibleAction.FETCH_PUNISHMENT_HISTORY)
                    .build());
                add(CommandNode.builder().keyword("pardon")
                    .handler(new CmdPunishmentsPardon(config))
                    .action(PermissibleAction.PARDON_POSSIBLY)
                    .build());
              }}
          )
          .build());

      add(CommandNode.builder()
          .keyword("players")
          .children(
              new HashSet<CommandNode>() {{
                add(CommandNode.builder().keyword("create")
                    .handler(new CmdPlayersCreate(config))
                    .build());
              }}
          )
          .build());

      add(CommandNode.builder()
          .keyword("bans")
          .action(PermissibleAction.BAN_PLAYER)
          .children(
              new HashSet<CommandNode>() {{
                add(CommandNode.builder().keyword("create")
                    .handler(new CmdBansCreate(config))
                    .action(PermissibleAction.BAN_PLAYER)
                    .build());
              }}
          )
          .build());

      add(CommandNode.builder()
          .keyword("mutes")
          .action(PermissibleAction.MUTE_PLAYER)
          .children(
              new HashSet<CommandNode>() {{
                add(CommandNode.builder().keyword("create")
                    .handler(new CmdMutesCreate(config))
                    .action(PermissibleAction.MUTE_PLAYER)
                    .build());
              }}
          )
          .build());

      add(CommandNode.builder()
          .keyword("warnings")
          .action(PermissibleAction.WARN_PLAYER)
          .children(
              new HashSet<CommandNode>() {{
                add(CommandNode.builder().keyword("create")
                    .handler(new CmdWarningsCreate(config))
                    .action(PermissibleAction.WARN_PLAYER)
                    .build());
              }}
          )
          .build());

      add(CommandNode.builder()
          .keyword("ipbans")
          .action(PermissibleAction.IP_BAN_PLAYER)
          .children(
              new HashSet<CommandNode>() {{
                add(CommandNode.builder().keyword("create")
                    .handler(new CmdIpBansCreate(config))
                    .action(PermissibleAction.IP_BAN_PLAYER)
                    .build());
              }}
          )
          .build());
    }};
  }

  @NonNull
  public ProcessorNode searchForHandler(CommandSender sender, String[] args) {
    return searchNodes(sender, nodes, args, 0);
  }

  @Override
  public List<String> onTabComplete(
      @NonNull CommandSender sender,
      @NonNull Command command,
      @NonNull String label,
      String @NonNull [] args
  ) {
    return searchForTabCompleteTerms(sender, CommandArgumentPreprocessor.preProcessArguments(new HashSet<>(), args), nodes, 0);
  }

  @NonNull
  private ProcessorNode searchNodes(CommandSender sender, Collection<CommandNode> nodes, String[] args, int argIndex) {
    String validOptionsString = nodesToString(sender, nodes);

    if (argIndex >= args.length) {
      throw error(String.format(NO_ARGS_MSG, validOptionsString));
    }

    String word = args[argIndex];

    for (CommandNode node : nodes) {
      if (node.matchesFully(word)) {
        if (node.getHandler() != null) {
          if (Optional.ofNullable(node.getAction()).map(action -> action.isAllowed(sender)).orElse(true)) {
            return ProcessorNode.builder().processor(node.getHandler()).argIndex(argIndex).build();
          }
          throw new CommandValidationException(NO_PERMS_MSG, false);
        } else {
          return searchNodes(sender, node.getChildren(), args, argIndex + 1);
        }
      }
    }

    List<CommandNode> candidates = nodes.stream()
        .filter(node -> node.matchesPartially(word)).collect(Collectors.toList());

    if (candidates.size() > 1) {
      throw error(String.format(AMBIGUOUS_WORD, word, nodesToString(sender, candidates)));
    } else if (candidates.size() == 1) {
      CommandNode node = candidates.get(0);
      if (node.getHandler() != null) {
        return ProcessorNode.builder().processor(node.getHandler()).argIndex(argIndex).build();
      } else {
        return searchNodes(sender, node.getChildren(), args, argIndex + 1);
      }
    }

    throw error(String.format(INVALID_WORD, word, validOptionsString));
  }

  private List<String> searchForTabCompleteTerms(
      CommandSender sender,
      ProcessedCommandArguments args,
      Collection<CommandNode> nodes,
      int argIndex
  ) {
    if (argIndex == args.getArgs().length) {
      return nodes.stream()
          .filter(node -> Optional.ofNullable(node.getAction()).map(action -> action.isAllowed(sender)).orElse(true))
          .map(CommandNode::getKeyword)
          .sorted()
          .collect(Collectors.toList());
    }

    String word = args.get(argIndex);

    for (CommandNode node : nodes) {
      if (node.matchesFully(word)) {
        if (argIndex == args.getArgs().length - 1 && !args.isFinishedTyping()) {
          return Collections.singletonList(word);
        } else if (node.getHandler() == null) {
          return searchForTabCompleteTerms(sender, args, node.getPermissibleChildren(sender), argIndex + 1);
        } else {
          ProcessedCommandArguments newArgs = ProcessedCommandArguments.forSubHandler(args, node.getHandler(), argIndex);
          return node.getHandler().onTabComplete(sender, newArgs);
        }
      }
    }

    List<CommandNode> partialMatches = nodes.stream()
        .filter(node -> node.matchesPartially(word))
        .collect(Collectors.toList());

    // i.e. if the user has pressed space and/or entered other arguments after this one
    if (args.isFinishedTyping() || args.getArgs().length > argIndex + 1) {
      // move on to the next node if possible
      if (partialMatches.size() == 1) {
        CommandNode node = partialMatches.get(0);
        if (node.getHandler() == null) {
          return searchForTabCompleteTerms(sender, args, node.getPermissibleChildren(sender), argIndex + 1);
        } else {
          ProcessedCommandArguments newArgs = ProcessedCommandArguments.forSubHandler(args, node.getHandler(), argIndex);
          return node.getHandler().onTabComplete(sender, newArgs);
        }
      }

      return new ArrayList<>();
    }

    // return all partial matches (there may be none)
    return nodes.stream()
        .filter(node -> node.matchesPartially(word))
        .filter(node -> Optional.ofNullable(node.getAction()).map(action -> action.isAllowed(sender)).orElse(true))
        .map(CommandNode::getKeyword)
        .sorted()
        .collect(Collectors.toList());
  }

  private String nodesToString(CommandSender sender, Collection<CommandNode> nodes) {
    return Arrays.toString(
        nodes.stream()
            .filter(node -> Optional.ofNullable(node.getAction()).map(action -> action.isAllowed(sender)).orElse(true))
            .map(CommandNode::getKeyword)
            .sorted().toArray()
    );
  }

  private CommandValidationException error(String message) {
    return new CommandValidationException(message, true);
  }

  @Builder
  public static class ProcessorNode {
    @Getter
    private final int argIndex;
    @Getter
    private final CommandProcessorBase processor;
  }
}
