package minealex.tchat.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class LevelsConfig {

    private final JavaPlugin plugin;

    public LevelsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "levels.yml");

        if (!configFile.exists()) {
            try {
                plugin.saveResource("levels.yml", false);
                plugin.getLogger().info("levels.yml created successfully.");
            } catch (IllegalArgumentException e) {
                // Captura y maneja excepciones al guardar recursos
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().info("levels.yml already exists.");
        }
    }

    public FileConfiguration getConfig() {
        File configFile = new File(plugin.getDataFolder(), "levels.yml");
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig(FileConfiguration config) {
        File configFile = new File(plugin.getDataFolder(), "levels.yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
