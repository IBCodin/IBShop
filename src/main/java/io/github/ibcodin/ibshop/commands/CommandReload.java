package io.github.ibcodin.ibshop.commands;

import io.github.ibcodin.ibshop.IBShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

public class CommandReload extends CommandHandler {
    public static final String CommandName = "ibshopreload";

    public CommandReload(final IBShop plugin) {
        super(plugin, CommandName);
    }

    @Override
    public void sendHelp(CommandSender sender, String label, boolean detailHelp) {
        if (senderHasPermission(sender)) {
            send(sender, CMD_RELOAD_HELP_1, label);
            send(sender, CMD_RELOAD_HELP_2);
            send(sender, CMD_RELOAD_HELP_3);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadAll();
        send(sender, CMD_RELOAD_DONE);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
