package minealex.tchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class HelpCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public HelpCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Obtener el archivo messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (messagesFile.exists()) {
            // Cargar la configuración desde messages.yml
            FileConfiguration config = YamlConfiguration.loadConfiguration(messagesFile);

            if (config.contains("Help")) {
                // Obtener la lista de reglas desde messages.yml
                List<String> rulesList = config.getStringList("Help");

                // Enviar cada regla
                for (String rule : rulesList) {
                    String coloredRule = ChatColor.translateAlternateColorCodes('&', rule);
                    sender.sendMessage(coloredRule);
                }
            } else {
                sender.sendMessage("Section 'Help' not found in 'messages.yml'.");
            }
        } else {
            sender.sendMessage("File 'messages.yml' not found.");
        }

        return true;
    }
}
