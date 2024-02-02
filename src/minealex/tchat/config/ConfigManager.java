package minealex.tchat.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final JavaPlugin plugin;
    private File commandFile;
    private FileConfiguration command;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        commandFile = new File(plugin.getDataFolder(), "commands.yml");

        if (!commandFile.exists()) {
            plugin.saveResource("commands.yml", false);
        }

        command = YamlConfiguration.loadConfiguration(commandFile);
    }

    public FileConfiguration getCommands() {
        return command;
    }

    public void saveConfig() {
        try {
            command.save(commandFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        commandFile = new File(plugin.getDataFolder(), "commands.yml");
        command = YamlConfiguration.loadConfiguration(commandFile);
    }
}
