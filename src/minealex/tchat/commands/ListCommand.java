package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public ListCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Obtener el archivo messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(messagesFile);

        // Obtener la configuración desde messages.yml
        String headerFormat = config.getString("List.header_format");
        String playerFormat = config.getString("List.player_format");
        String footerFormat = config.getString("List.footer_format");

        if (headerFormat != null && playerFormat != null && footerFormat != null) {
            headerFormat = ChatColor.translateAlternateColorCodes('&', headerFormat);
            playerFormat = ChatColor.translateAlternateColorCodes('&', playerFormat);
            footerFormat = ChatColor.translateAlternateColorCodes('&', footerFormat);

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
            sender.sendMessage("Header, player, or footer format not found in messages.yml.");
        }

        return true;
    }
}
