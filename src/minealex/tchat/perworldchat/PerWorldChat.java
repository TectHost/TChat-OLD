package minealex.tchat.perworldchat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PerWorldChat implements Listener {
    private WorldsManager worldsManager;
    private JsonObject formatConfig;
    private Gson gson;
    private File configFile;

    public PerWorldChat(Plugin plugin) {
        this.worldsManager = new WorldsManager(new File(plugin.getDataFolder(), "worlds.json"));
        this.formatConfig = loadFormatConfig(new File(plugin.getDataFolder(), "format_config.json"));
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.configFile = new File(plugin.getDataFolder(), "worlds.json");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                initConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        WorldConfig worldConfig = worldsManager.loadWorldConfig(worldName);

        if (!worldConfig.isChatEnabled()) {
            String chatDisabledMessage = formatConfig.get("messages").getAsJsonObject().get("chatDisabledMessage").getAsString();
            chatDisabledMessage = ChatColor.translateAlternateColorCodes('&', chatDisabledMessage);
            player.sendMessage(chatDisabledMessage);
            event.setCancelled(true);
            return;
        }

        if (!worldConfig.isPerWorldChat()) {
            return;
        }

        Set<Player> r = event.getRecipients();
        Iterator<Player> iterator = r.iterator();

        while (iterator.hasNext()) {
            Player recipient = iterator.next();
            if (!recipient.getWorld().getName().equals(player.getWorld().getName())) {
                iterator.remove();
            }
        }
    }

    private JsonObject loadFormatConfig(File configFile) {
        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            return parser.parse(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            List<WorldConfig> defaultConfigs = new ArrayList<>();
            defaultConfigs.add(new WorldConfig("world", true, false, false, 10));
            defaultConfigs.add(new WorldConfig("world_nether", true, false, false, 10));
            defaultConfigs.add(new WorldConfig("world_the_end", true, false, false, 10));
            String json = gson.toJson(defaultConfigs);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}