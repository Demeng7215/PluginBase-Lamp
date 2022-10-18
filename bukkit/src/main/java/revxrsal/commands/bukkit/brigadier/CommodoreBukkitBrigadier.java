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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.BukkitBrigadier;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.core.BukkitActor;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.util.ClassMap;
import revxrsal.commands.util.Preconditions;

public final class CommodoreBukkitBrigadier implements BukkitBrigadier {

  private final BukkitCommandHandler handler;

  private final Commodore commodore;
  private final ClassMap<ArgumentTypeResolver> argumentTypes = new ClassMap<>();

  public CommodoreBukkitBrigadier(BukkitCommandHandler handler) {
    this.handler = handler;
    commodore = CommodoreProvider.getCommodore(handler.getPlugin());
    if (CommodoreProvider.isSupported()) {
      bind(String.class, DefaultArgTypeResolvers.STRING);
      bind(Number.class, DefaultArgTypeResolvers.NUMBER);
      bind(Boolean.class, DefaultArgTypeResolvers.BOOLEAN);
      bind(Player.class, DefaultArgTypeResolvers.PLAYER);
      bind(EntitySelector.class, DefaultArgTypeResolvers.ENTITY_SELECTOR);
    }
  }

  @Override
  public void bind(@NotNull Class<?> type, @NotNull ArgumentTypeResolver resolver) {
    Preconditions.notNull(type, "type");
    Preconditions.notNull(resolver, "resolver");
    argumentTypes.add(type, resolver);
  }

  @Override
  public void bind(@NotNull Class<?> type, @NotNull ArgumentType<?> argumentType) {
    Preconditions.notNull(type, "type");
    Preconditions.notNull(argumentType, "argument type");
    argumentTypes.add(type, parameter -> argumentType);
  }

  @Override
  public void bind(@NotNull Class<?> type, @NotNull MinecraftArgumentType argumentType) {
    Preconditions.notNull(type, "type");
    Preconditions.notNull(argumentType, "argument type");
    argumentType.getIfPresent().ifPresent(c -> argumentTypes.add(type, parameter -> c));
  }

  public @NotNull ArgumentType<?> getArgumentType(@NotNull CommandParameter parameter) {
    ArgumentTypeResolver resolver = argumentTypes.getFlexible(parameter.getType());
    if (resolver != null) {
      ArgumentType<?> type = resolver.getArgumentType(parameter);
      if (type != null) {
        return type;
      }
    }
    return StringArgumentType.string();
  }

  private void checkSupported() {
    if (commodore == null) {
      throw new IllegalArgumentException("Brigadier is not supported on this version.");
    }
  }

  @Override
  public @NotNull CommandActor wrapSource(@NotNull Object commandSource) {
    checkSupported();
    return new BukkitActor(commodore.getBukkitSender(commandSource), handler);
  }

  @Override
  public void register() {
    if (!CommodoreProvider.isSupported()) {
      return;
    }
    BrigadierTreeParser.parse(this, handler).forEach(n -> register(n.build()));
  }

  private void register(@NotNull LiteralCommandNode<?> node) {
    Command command = ((JavaPlugin) handler.getPlugin()).getCommand(node.getLiteral());
    if (command == null) {
      commodore.register(node);
    } else {
      commodore.register(command, node);
    }
  }
}
