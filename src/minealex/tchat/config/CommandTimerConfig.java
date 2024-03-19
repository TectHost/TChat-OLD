package minealex.tchat.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class CommandTimerConfig {

    private final JavaPlugin plugin;

    public CommandTimerConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "command_timer.yml");

        if (!configFile.exists()) {
            try {
                plugin.saveResource("command_timer.yml", false);
                plugin.getLogger().info("command_timer.yml created successfully.");
            } catch (IllegalArgumentException e) {
                // Captura y maneja excepciones al guardar recursos
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().info("command_timer.yml already exists.");
        }
    }

    public FileConfiguration getConfig() {
        File configFile = new File(plugin.getDataFolder(), "command_timer.yml");
        return YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig(FileConfiguration config) {
        File configFile = new File(plugin.getDataFolder(), "command_timer.yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
