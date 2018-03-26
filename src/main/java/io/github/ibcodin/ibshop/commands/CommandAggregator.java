package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommandAggregator extends CommandHandler {

    protected final transient List<AlternateCommand> kids = new ArrayList<>();

    protected CommandAggregator(IBShop plugin, String commandName) {
        super(plugin, commandName);
    }

    public void addKid(CommandHandler handler) {
        String extra = handler.getCommandName().substring(this.getCommandName().length());
        kids.add(new AlternateCommand(handler, extra));
    }

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

        public boolean isCommand(String extra) {
            return extra.equalsIgnoreCase(this.extra);
        }

        public boolean canCommand(CommandSender sender) {
            return handler.senderHasPermission(sender);
        }

        public boolean wantsCommand(CommandSender sender, String extra) {
            return canCommand(sender) && isCommand(extra);
        }

        public void sendHelp(CommandSender sender, String label, final boolean detailHelp) {
            handler.sendHelp(sender, label + " " + extra, detailHelp);
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
