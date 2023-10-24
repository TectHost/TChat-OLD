package minealex.tchat.perworldchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldsManager {
    private Gson gson;
    private File configFile;

    public WorldsManager(File configFile) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
        try (FileWriter writer = new FileWriter(configFile)) {
            List<WorldConfig> defaultConfigs = new ArrayList<>();
            defaultConfigs.add(new WorldConfig("world", true));
            String json = gson.toJson(defaultConfigs);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public WorldConfig loadWorldConfig(String worldName) {
        List<WorldConfig> worldConfigs = loadWorldConfigs();

        for (WorldConfig config : worldConfigs) {
            if (config.getWorldName().equals(worldName)) {
                return config;
            }
        }

        return new WorldConfig(worldName, true); // Si no se encuentra, por defecto está habilitado
    }

    public List<WorldConfig> loadWorldConfigs() {
        try (FileReader reader = new FileReader(configFile)) {
            WorldConfig[] worldConfigs = gson.fromJson(reader, WorldConfig[].class);

            if (worldConfigs != null) {
                return new ArrayList<>(Arrays.asList(worldConfigs));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public void saveWorldConfigs(List<WorldConfig> worldConfigs) {
        try (FileWriter writer = new FileWriter(configFile)) {
            String json = gson.toJson(worldConfigs);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
