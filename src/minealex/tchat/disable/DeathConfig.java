package minealex.tchat.disable;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DeathConfig {

    private final JavaPlugin plugin;

    public DeathConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "death_messages.json");

        if (!configFile.exists()) {
            try {
                plugin.saveResource("death_messages.json", false);
                plugin.getLogger().info("death_messages.json created successfully.");
            } catch (IllegalArgumentException e) {
                // Captura y maneja excepciones al guardar recursos
                e.printStackTrace();
            }
        } else {
            plugin.getLogger().info("death_messages.json already exists.");
        }
    }

    public FileConfiguration getConfig() {
        File configFile = new File(plugin.getDataFolder(), "death_messages.json");
        return YamlConfiguration.loadConfiguration(configFile);
    }
}
