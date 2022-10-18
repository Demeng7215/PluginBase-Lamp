/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copyright (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.bukkit.brigadier;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitBrigadier;
import revxrsal.commands.bukkit.core.BukkitHandler;
import revxrsal.commands.command.ArgumentStack;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.ArgumentParseException;

/**
 * A utility class for parsing Lamp's components into Brigadier's.
 */
@SuppressWarnings("rawtypes")
public final class BrigadierTreeParser {

  private static final Command NO_ACTION = context -> Command.SINGLE_SUCCESS;

  /**
   * Parses all the registered commands and categories in the given {@link CommandHandler} and
   * registers all root trees and their corresponding children components and parameters
   *
   * @param brigadier The platform's Brigadier implementation
   * @param handler   The command handler
   * @return All root nodes
   */
  public static <T> List<LiteralArgumentBuilder<T>> parse(
      @NotNull BukkitBrigadier brigadier,
      @NotNull CommandHandler handler
  ) {
    List<LiteralArgumentBuilder<T>> nodes = new ArrayList<>();
    List<CommandCategory> roots = handler.getCategories().values().stream()
        .filter(c -> c.getPath().isRoot()).collect(Collectors.toList());
    List<ExecutableCommand> rootCommands = handler.getCommands().values().stream()
        .filter(c -> c.getPath().isRoot()).collect(Collectors.toList());
    for (CommandCategory root : roots) {
      nodes.add(parse(brigadier, literal(root.getName()), root));
    }
    for (ExecutableCommand root : rootCommands) {
      nodes.add(parse(brigadier, literal(root.getName()), root));
    }
    return nodes;
  }

  /**
   * Parses the given command category into a {@link LiteralArgumentBuilder}.
   *
   * @param brigadier The platform's Brigadier implementation
   * @param into      The command node to register nodes into
   * @param category  Category to parse
   * @return The parsed command node
   */
  public static <T> LiteralArgumentBuilder<T> parse(BukkitBrigadier brigadier,
      LiteralArgumentBuilder<?> into, CommandCategory category) {
    for (CommandCategory child : category.getCategories().values()) {
      LiteralArgumentBuilder childLiteral = parse(brigadier, literal(child.getName()), child);
      into.then(childLiteral);
    }
    for (ExecutableCommand child : category.getCommands().values()) {
      LiteralArgumentBuilder childLiteral = parse(brigadier, literal(child.getName()), child);
      into.then(childLiteral);
    }
    if (category.getDefaultAction() != null) {
      parse(brigadier, into, category.getDefaultAction());
    }
    into.requires(a -> category.hasPermission(brigadier.wrapSource(a)));
    return (LiteralArgumentBuilder<T>) into;
  }

  /**
   * Parses the given command into a {@link LiteralArgumentBuilder}.
   *
   * @param brigadier The platform's Brigadier implementation
   * @param into      The command node to register nodes into
   * @param command   Command to parse
   * @return The parsed command node
   */
  public static <T> LiteralArgumentBuilder<T> parse(BukkitBrigadier brigadier,
      LiteralArgumentBuilder<?> into,
      ExecutableCommand command) {
    CommandNode lastParameter = null;
    List<CommandParameter> sortedParameters = new ArrayList<>(
        command.getValueParameters().values());
    Collections.sort(sortedParameters);
    for (int i = 0; i < sortedParameters.size(); i++) {
      boolean isLast = i == sortedParameters.size() - 1;
      CommandParameter parameter = sortedParameters.get(i);
      if (parameter.isFlag()) {
        break;
      }
      ArgumentBuilder<?, ?> builder = getBuilder(brigadier, command, parameter);
      if (!isLast && sortedParameters.get(i + 1).isOptional()) {
        builder.executes(NO_ACTION);
      }
      if (lastParameter == null) {
        if (parameter.isOptional()) {
          into.executes(NO_ACTION);
        }
        into.then(lastParameter = builder.build());
      } else {
        lastParameter.addChild(lastParameter = builder.build());
      }
    }
    sortedParameters.removeIf(parameter -> !parameter.isFlag());
    CommandNode next = null;
    for (CommandParameter parameter : sortedParameters) {
      if (next == null) {
        if (lastParameter == null) {
          into.then(next = literal(
              parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName()).build());
        } else {
          lastParameter.addChild(next = literal(
              parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName()).build());
        }
      } else {
        next.addChild(next = literal(
            parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName()).build());
      }
      next.addChild(next = getBuilder(brigadier, command, parameter).build());
    }
    into.requires(a -> command.hasPermission(brigadier.wrapSource(a)));
    return (LiteralArgumentBuilder<T>) into;
  }

  private static ArgumentBuilder getBuilder(BukkitBrigadier brigadier,
      ExecutableCommand command,
      CommandParameter parameter) {
    if (parameter.isSwitch()) {
      return literal(parameter.getCommandHandler().getSwitchPrefix() + parameter.getSwitchName());
    }
    ArgumentType<?> argumentType = brigadier.getArgumentType(parameter);

    return argument(parameter.getName(), argumentType)
        .requires(a -> parameter.hasPermission(brigadier.wrapSource(a)))
        .suggests(createSuggestionProvider(brigadier, command, parameter));
  }

  private static SuggestionProvider<Object> createSuggestionProvider(
      BukkitBrigadier brigadier,
      ExecutableCommand command,
      CommandParameter parameter
  ) {
    if (parameter.getSuggestionProvider()
        == revxrsal.commands.autocomplete.SuggestionProvider.EMPTY) {
      return null;
    }
    if (parameter.getSuggestionProvider() == BukkitHandler.playerSuggestionProvider) {
      return null;
    }
    return (context, builder) -> {
      try {
        CommandActor actor = brigadier.wrapSource(context.getSource());
        String tooltipMessage =
            parameter.getDescription() == null ? parameter.getName() : parameter.getDescription();
        Message tooltip = new LiteralMessage(tooltipMessage);
        String input = context.getInput();
        try {
          ArgumentStack args = parameter.getCommandHandler().parseArgumentsForCompletion(
              input.startsWith("/") ? input.substring(1) : input
          );
          parameter.getSuggestionProvider().getSuggestions(args, actor, command)
              .stream()
              .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
              .sorted(String.CASE_INSENSITIVE_ORDER)
              .distinct()
              .forEach(c -> builder.suggest(c, tooltip));
        } catch (ArgumentParseException ignore) {
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
      return builder.buildFuture();
    };
  }
}
