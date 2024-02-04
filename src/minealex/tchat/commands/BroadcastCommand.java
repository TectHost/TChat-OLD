package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import minealex.tchat.TChat;

public class BroadcastCommand implements CommandExecutor {

    private final TChat plugin;

    public BroadcastCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tchat.broadcast")) {
            sender.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            return true;
        }

        if (args.length == 0) {
        	sender.sendMessage(plugin.getMessagesYML("messages.broadcastUsage"));
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String word : args) {
            message.append(word).append(" ");
        }

        // Obtener la configuración desde config.yml
        FileConfiguration config = plugin.getConfig();

        if (config.contains("Announcements.broadcastFormat")) {
            String format = config.getString("Announcements.broadcastFormat");

            String formattedMessage = ChatColor.translateAlternateColorCodes('&', format.replace("%s", message.toString().trim()));
            Bukkit.broadcastMessage(formattedMessage);
        } else {
            sender.sendMessage("Broadcast format not found in config.yml.");
        }

        return true;
    }
}
