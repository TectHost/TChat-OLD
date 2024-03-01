package minealex.tchat.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class BannedCommandsConfig {

    private final JavaPlugin plugin;

    public BannedCommandsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "banned_commands.yml");

        if (!configFile.exists()) {
            try {
                plugin.saveResource("banned_commands.yml", false);
                plugin.getLogger().info("banned_commands.yml created successfully.");
            } catch (IllegalArgumentException e) {
                // Captura y maneja excepciones al guardar recursos
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().info("banned_commands.yml already exists.");
        }
    }

    public FileConfiguration getConfig() {
        File configFile = new File(plugin.getDataFolder(), "banned_commands.yml");
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig(FileConfiguration config) {
        File configFile = new File(plugin.getDataFolder(), "banned_commands.yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
