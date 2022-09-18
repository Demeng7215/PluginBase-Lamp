package revxrsal.commands.bukkit.core;

import static revxrsal.commands.util.Preconditions.notNull;
import static revxrsal.commands.util.Strings.colorize;

import dev.demeng.pluginbase.lib.adventure.audience.Audience;
import dev.demeng.pluginbase.lib.adventure.text.ComponentLike;
import dev.demeng.pluginbase.locale.Locales;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.exception.SenderNotConsoleException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;

@Internal
public final class BukkitActor implements BukkitCommandActor {

    private static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes("CONSOLE".getBytes(StandardCharsets.UTF_8));

    private final CommandSender sender;
    private final BukkitHandler handler;

    public BukkitActor(CommandSender sender, CommandHandler handler) {
        this.sender = notNull(sender, "sender");
        this.handler = (BukkitHandler) notNull(handler, "handler");
    }

    @Override public @NotNull CommandSender getSender() {
        return sender;
    }

    @Override public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override public boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    @Override public @Nullable Player getAsPlayer() {
        return isPlayer() ? (Player) sender : null;
    }

    @Override public @NotNull Player requirePlayer() {
        if (!isPlayer())
            throw new SenderNotPlayerException();
        return (Player) sender;
    }

    @Override public @NotNull ConsoleCommandSender requireConsole() {
        if (!isConsole())
            throw new SenderNotConsoleException();
        return (ConsoleCommandSender) sender;
    }

    @Override public @NotNull Audience audience() {
        if (sender instanceof Audience)
            return (Audience) sender;
        return handler.bukkitAudiences.sender(getSender());
    }

    @Override public void reply(@NotNull ComponentLike component) {
        audience().sendMessage(component);
    }

    @Override public @NotNull String getName() {
        return sender.getName();
    }

    @Override public @NotNull UUID getUniqueId() {
        if (isPlayer())
            return ((Player) sender).getUniqueId();
        else if (isConsole())
            return CONSOLE_UUID;
        else
            return UUID.nameUUIDFromBytes(getName().getBytes(StandardCharsets.UTF_8));
    }

    @Override public void reply(@NotNull String message) {
        notNull(message, "message");
        sender.sendMessage(colorize(handler.getMessagePrefix() + message));
    }

    @Override public void error(@NotNull String message) {
        notNull(message, "message");
        sender.sendMessage(colorize(handler.getMessagePrefix() + "&c" + message));
    }

    @Override public BukkitCommandHandler getCommandHandler() {
        return handler;
    }

    @Override public @NotNull Locale getLocale() {
        if (isPlayer()) {
            String playerLocale;
            try {
                playerLocale = requirePlayer().getLocale();
            } catch (NoSuchMethodError e) {
                try {
                    playerLocale = requirePlayer().spigot().getLocale();
                } catch (NoSuchMethodError e2) {
                    return BukkitCommandActor.super.getLocale();
                }
            }
            Locale locale = Locales.get(playerLocale);
            return locale == null ? BukkitCommandActor.super.getLocale() : locale;
        }
        return BukkitCommandActor.super.getLocale();
    }
}
