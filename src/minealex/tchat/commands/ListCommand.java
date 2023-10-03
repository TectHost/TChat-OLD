package minealex.tchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ListCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ListCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            File configFile = new File(plugin.getDataFolder(), "format_config.json");

            if (configFile.exists()) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(configFile));

                JSONObject listObject = (JSONObject) jsonObject.get("List");

                if (listObject != null) {
                    String headerFormat = (String) listObject.get("header_format");
                    String playerFormat = (String) listObject.get("player_format");
                    String footerFormat = (String) listObject.get("footer_format");

                    if (headerFormat != null && playerFormat != null && footerFormat != null) {
                        headerFormat = headerFormat.replace("&", "§"); 
                        playerFormat = playerFormat.replace("&", "§");
                        footerFormat = footerFormat.replace("&", "§");
                        
                        List<String> onlinePlayers = new ArrayList<>();
                        for (Player player : plugin.getServer().getOnlinePlayers()) {
                            onlinePlayers.add(player.getName());
                        }

                        sender.sendMessage(headerFormat.replace("%online%", String.valueOf(onlinePlayers.size())).replace("%max%", String.valueOf(plugin.getServer().getMaxPlayers())));

                        // Construir el mensaje con comas
                        StringBuilder messageBuilder = new StringBuilder();
                        for (int i = 0; i < onlinePlayers.size(); i++) {
                            String playerName = onlinePlayers.get(i);
                            messageBuilder.append(playerFormat.replace("%player%", playerName));

                            if (i < onlinePlayers.size() - 1) {
                                messageBuilder.append(", ");
                            }
                        }

                        sender.sendMessage(messageBuilder.toString());
                        sender.sendMessage(footerFormat);
                    } else {
                        sender.sendMessage("Header, player, or footer format not found in format_config.json.");
                    }
                } else {
                    sender.sendMessage("List section not found in format_config.json.");
                }
            } else {
                sender.sendMessage("File 'format_config.json' was not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
