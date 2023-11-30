package minealex.tchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import minealex.tchat.TChat;

import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

public class AdminChatCommand implements CommandExecutor {

    private final TChat plugin;
    private String adminChatFormat;
    private String adminChatJoinMessage;
    private String adminChatLeaveMessage;

    public AdminChatCommand(TChat plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("tchat.adminchat")) {
                loadConfig(); // Reload the config to make sure variables are updated

                if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
                    removePlayerFromAdminChat(player);
                    String message = String.join(" ", args);

                    // Check if variables are not null before using them
                    if (adminChatFormat != null) {
                        for (UUID uuid : plugin.getStaffChatPlayers()) {
                            Player adminPlayer = Bukkit.getPlayer(uuid);
                            if (adminPlayer != null && adminPlayer.isOnline()) {
                                adminPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', adminChatFormat
                                        .replace("%player%", player.getName())
                                        .replace("%message%", message)
                                ));
                            }
                        }
                    }
                    return true;
                } else {
                    plugin.addPlayerToStaffChat(player);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', adminChatJoinMessage));
                    return true;
                }
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getOnlyPlayer()));
        }
        return false;
    }

    public void removePlayerFromAdminChat(Player player) {
        if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
            plugin.removePlayerFromStaffChat(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', adminChatLeaveMessage));
        }
    }

    private String getOnlyPlayer() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject config = (JSONObject) parser.parse(new FileReader("plugins/TChat/format_config.json"));
            JSONObject messagesConfig = (JSONObject) config.get("messages");

            return (String) messagesConfig.get("playersOnly");
        } catch (IOException | org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            return "An error occurred while loading the message to players.";
        }
    }

    private void loadConfig() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject config = (JSONObject) parser.parse(new FileReader("plugins/TChat/format_config.json"));
            JSONObject adminConfig = (JSONObject) config.get("admin");

            // Leer configuraciones del JSON
            adminChatFormat = (String) adminConfig.get("format");
            adminChatJoinMessage = (String) adminConfig.get("admin_chat_join_message");
            adminChatLeaveMessage = (String) adminConfig.get("admin_chat_leave_message");
        } catch (IOException | org.json.simple.parser.ParseException e) {
            System.out.println("Error al cargar la configuración: " + e.getMessage());
        }
    }
}
