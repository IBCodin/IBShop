package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import io.github.ibcodin.ibshop.MessageLookup;
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
    protected final transient List<AlternateCommand> kids = new ArrayList<>();
    private final String commandName;

    protected CommandHandler(IBShop plugin, String commandName) {
        this.plugin = plugin;
        this.commandName = commandName;
        this.command = plugin.getCommand(commandName);
        this.command.setExecutor(this);
        this.command.setTabCompleter(this);
    }

    protected void sendMessage(final CommandSender sender, final String message) {
        sender.sendMessage(plugin.getSettings().prefixMessage(message));
    }

    protected void sendMessage(final CommandSender sender, final MessageLookup.IBShopMessages message, Object... args) {
        plugin.getMessageLookup().sendMessage(sender, message, args);
    }

    protected void log(Level level, String message) {
        plugin.getLogger().log(level, message);
    }

    protected boolean senderHasPermission(final CommandSender sender) {
        return sender.hasPermission(command.getPermission());
    }

    public void addKid(CommandHandler handler, String extra) {
        kids.add(new AlternateCommand(handler, extra));
    }

    protected boolean hasKids() {
        return kids.size() > 0;
    }

    public abstract void sendHelp(final CommandSender sender, final String label, final boolean detailHelp);

    protected static class AlternateCommand {
        final private CommandHandler handler;
        final private String extra;

        public AlternateCommand(CommandHandler handler, String extra) {
            this.handler = handler;
            this.extra = extra;
        }

        public String getExtra() {
            return this.extra;
        }

        public void sendHelp(CommandSender sender, String label, final boolean detailHelp) {
            handler.sendHelp(sender, label + " " + extra, detailHelp);
        }

        public boolean isCommand(String extra) {
            return extra.equalsIgnoreCase(this.extra);
        }

        public boolean canCommand(CommandSender sender) {
            return handler.senderHasPermission(sender);
        }

        public boolean wantsCommand(CommandSender sender, String extra) {
            return canCommand(sender) && isCommand(extra);
        }

        public boolean sendCommand(CommandSender sender, String label, String[] args) {
            String[] myArgs = {};
            if (args.length > 1) {
                myArgs = Arrays.copyOfRange(args, 1, args.length);
            }
            return handler.onCommand(sender, handler.command, label + " " + extra, myArgs);
        }

        public List<String> getTabComplete(CommandSender sender, String label, String[] args) {
            String[] myStrings = {};
            if (args.length > 1) {
                myStrings = Arrays.copyOfRange(args, 1, args.length);
            }
            return handler.onTabComplete(sender, handler.command, label + " " + extra, myStrings);
        }
    }
}
