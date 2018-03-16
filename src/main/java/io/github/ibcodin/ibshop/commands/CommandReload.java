package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends CommandHandler {
    public static final String CommandName = "ibshopreload";

    public CommandReload(final IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (senderHasPermission(sender)) {
            sendMessage(sender, "/" + label);
            sendMessage(sender, ChatColor.YELLOW + "  Rereads plugin configuration from disk");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        plugin.getSettings().reload();

        plugin.getMessageLookup().reload();

        plugin.getItemLookup().reload();

        plugin.getBlackList().reload();

        plugin.getSalesList().reload();

        sender.sendMessage(plugin.getSettings().prefixMessage("reloaded"));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
