package io.github.ibcodin.ibshop;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static io.github.ibcodin.ibshop.IBShopMessages.*;

@Deprecated
public class MessageLookup {

    private final IBShop plugin;

    public MessageLookup(IBShop plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(CommandSender sender, IBShopMessages msg, Object... args) {
        plugin.send(sender, msg, args);
    }

    protected void log(Level level, String message) {
        plugin.log(level, message);
    }

}
