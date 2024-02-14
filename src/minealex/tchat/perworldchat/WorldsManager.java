package minealex.tchat.perworldchat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorldsManager {
    private FileConfiguration config;
    private File configFile;

    public WorldsManager(File configFile) {
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.configFile = configFile;

        // Crea el archivo si no existe
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                // Si el archivo se crea por primera vez, inicializa la configuración
                initConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initConfig() {
        List<WorldConfig> defaultConfigs = new ArrayList<>();
        defaultConfigs.add(new WorldConfig("world", true, false, false, 10));
        defaultConfigs.add(new WorldConfig("world_nether", true, false, false, 10));
        defaultConfigs.add(new WorldConfig("world_the_end", true, false, false, 10));

        for (WorldConfig worldConfig : defaultConfigs) {
            saveWorldConfig(worldConfig);
        }
    }

    public WorldConfig loadWorldConfig(String worldName) {
        if (config.isConfigurationSection(worldName)) {
            ConfigurationSection section = config.getConfigurationSection(worldName);
            return new WorldConfig(
                    worldName,
                    section.getBoolean("chatEnabled", true),
                    section.getBoolean("perWorldChat", false),
                    section.getBoolean("radiusChatEnabled", false),
                    section.getInt("radiusChat", 10)
            );
        }

        return new WorldConfig(worldName, true, false, false, 10);
    }

    public List<WorldConfig> loadWorldConfigs() {
        List<WorldConfig> worldConfigs = new ArrayList<>();

        Set<String> worldNames = config.getKeys(false);
        for (String worldName : worldNames) {
            WorldConfig worldConfig = loadWorldConfig(worldName);
            worldConfigs.add(worldConfig);
        }

        return worldConfigs;
    }

    public void saveWorldConfig(WorldConfig worldConfig) {
        String worldName = worldConfig.getWorldName();
        config.set(worldName + ".chatEnabled", worldConfig.isChatEnabled());
        config.set(worldName + ".perWorldChat", worldConfig.isPerWorldChat());
        config.set(worldName + ".radiusChatEnabled", worldConfig.isRadiusChatEnabled());
        config.set(worldName + ".radiusChat", worldConfig.getRadiusChat());

        saveConfig();
    }

    public void saveWorldConfigs(List<WorldConfig> worldConfigs) {
        for (WorldConfig worldConfig : worldConfigs) {
            saveWorldConfig(worldConfig);
        }
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}