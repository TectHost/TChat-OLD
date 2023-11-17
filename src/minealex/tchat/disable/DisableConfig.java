package minealex.tchat.disable;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DisableConfig {

    private final JavaPlugin plugin;

    public DisableConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "disable.json");

        if (!configFile.exists()) {
            plugin.saveResource("disable.json", false);
            plugin.getLogger().info("disable.json created successfully.");
        } else {
            plugin.getLogger().info("disable.json already exists.");
        }
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
