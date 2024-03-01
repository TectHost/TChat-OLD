package minealex.tchat.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class DisableConfig {

    private final JavaPlugin plugin;

    public DisableConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "disable.yml");

        if (!configFile.exists()) {
            try {
                plugin.saveResource("disable.yml", false);
                plugin.getLogger().info("disable.yml created successfully.");
            } catch (IllegalArgumentException e) {
                // Captura y maneja excepciones al guardar recursos
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().info("disable.yml already exists.");
        }
    }

    public FileConfiguration getConfig() {
        File configFile = new File(plugin.getDataFolder(), "disable.yml");
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig(FileConfiguration config) {
        File configFile = new File(plugin.getDataFolder(), "disable.yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
