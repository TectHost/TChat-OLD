package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import minealex.tchat.TChat;

public class AnnouncementCommand implements CommandExecutor {

    private final TChat plugin;

    public AnnouncementCommand(TChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tchat.announcement")) {
            sender.sendMessage(plugin.getMessagesYML("messages.noPermission"));
            return true;
        }

        if (args.length == 0) {
        	sender.sendMessage(plugin.getMessagesYML("messages.announcementUsage"));
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (String word : args) {
            message.append(word).append(" ");
        }

        // Obtener la configuración desde config.yml
        FileConfiguration config = plugin.getConfig();

        if (config.contains("Announcements.announcementFormat")) {
            String format = config.getString("Announcements.announcementFormat");

            String formattedMessage = ChatColor.translateAlternateColorCodes('&', format.replace("%s", message.toString().trim()));
            Bukkit.broadcastMessage(formattedMessage);
        } else {
            sender.sendMessage("Announcement format not found in config.yml.");
        }

        return true;
    }
}
