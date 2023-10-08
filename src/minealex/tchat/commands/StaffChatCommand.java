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

public class StaffChatCommand implements CommandExecutor {

    private final TChat plugin;
    private String staffChatFormat;
    private String staffChatJoinMessage;
    private String staffChatLeaveMessage;

    public StaffChatCommand(TChat plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
        	Player player = (Player) sender;
            if (player.hasPermission("tchat.staffchat")) {
                if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
                	removePlayerFromStaffChat(player);
                    String message = String.join(" ", args);
                    for (UUID uuid : plugin.getStaffChatPlayers()) {
                        Player staffPlayer = Bukkit.getPlayer(uuid);
                        if (staffPlayer != null && staffPlayer.isOnline()) {
                        	player.sendMessage(ChatColor.translateAlternateColorCodes('&', staffChatFormat
                        	        .replace("%player%", player.getName())
                        	        .replace("%message%", message)
                        	));
                        }
                    }
                    return true;
                } else {
                    plugin.addPlayerToStaffChat(player);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', staffChatJoinMessage));
                    return true;
                }
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getOnlyPlayer()));
        }
        return false;
    }
    
    public void removePlayerFromStaffChat(Player player) {
        if (plugin.getStaffChatPlayers().contains(player.getUniqueId())) {
            plugin.removePlayerFromStaffChat(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', staffChatLeaveMessage));
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
            return "Ocurrió un error al cargar el mensaje para jugadores.";
        }
    }
    
    private void loadConfig() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject config = null;
            try {
                config = (JSONObject) parser.parse(new FileReader("plugins/TChat/format_config.json"));
            } catch (org.json.simple.parser.ParseException e) {
                e.printStackTrace();
            }
            JSONObject staffConfig = (JSONObject) config.get("Staff");

            // Leer configuraciones del JSON
            staffChatFormat = (String) staffConfig.get("format");
            staffChatJoinMessage = (String) staffConfig.get("staff_chat_join_message");
            staffChatLeaveMessage = (String) staffConfig.get("staff_chat_leave_message");
        } catch (IOException e) {
            System.out.println("Error al cargar la configuración: " + e.getMessage());
        }
    }

}
