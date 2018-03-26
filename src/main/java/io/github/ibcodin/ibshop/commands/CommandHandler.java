package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import io.github.ibcodin.ibshop.IBShopMessages;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class CommandHandler implements CommandExecutor, TabCompleter {
    protected final IBShop plugin;
    protected final PluginCommand command;
    private final String commandName;

    protected CommandHandler(IBShop plugin, String commandName) {
        this.plugin = plugin;
        this.commandName = commandName;
        this.command = plugin.getCommand(commandName);
        this.command.setExecutor(this);
        this.command.setTabCompleter(this);
    }

    protected void send(final CommandSender sender, final String message) {
        plugin.send(sender, message);
    }

    protected void send(final CommandSender sender, final IBShopMessages message, Object... args) {
        plugin.send(sender, message, args);
    }

    protected void log(Level level, String message) {
        plugin.log(level, message);
    }

    protected boolean senderHasPermission(final CommandSender sender) {
        return sender.hasPermission(command.getPermission());
    }

    public abstract void sendHelp(final CommandSender sender, final String label, final boolean detailHelp);

    public final String getCommandName() {
        return commandName;
    }
}
