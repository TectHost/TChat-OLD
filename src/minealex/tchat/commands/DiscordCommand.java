package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

public class DiscordCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public DiscordCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tchat.discord")) {
            sendNoPermissionMessage(sender);
            return true;
        }

        try {
            File configFile = new File(plugin.getDataFolder(), "format_config.json");

            if (configFile.exists()) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(configFile));

                JSONArray rulesArray = (JSONArray) jsonObject.get("discord_media");

                // Enviar cada regla
                for (Object rule : rulesArray) {
                    String coloredRule = ChatColor.translateAlternateColorCodes('&', (String) rule);
                    sender.sendMessage(coloredRule);
                }
            } else {
                sender.sendMessage("File 'format_config.json' was not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void sendNoPermissionMessage(CommandSender sender) {
        try {
            File configFile = new File(plugin.getDataFolder(), "format_config.json");

            if (configFile.exists()) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(configFile));

                JSONObject messagesObject = (JSONObject) jsonObject.get("messages");
                if (messagesObject != null) {
                    String noPermissionMessage = (String) messagesObject.get("noPermission");
                    if (noPermissionMessage != null && !noPermissionMessage.isEmpty()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Mensaje predeterminado si hay un error o el mensaje no está configurado
        sender.sendMessage(ChatColor.RED + "Error in format_config.json or noPermission message not configured.");
    }
}
