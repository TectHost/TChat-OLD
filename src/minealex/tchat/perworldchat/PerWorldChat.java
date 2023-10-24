package minealex.tchat.perworldchat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PerWorldChat implements Listener {
    private WorldsManager worldsManager;
    private JsonObject formatConfig;

    public PerWorldChat(Plugin plugin) {
        this.worldsManager = new WorldsManager(new File(plugin.getDataFolder(), "worlds.json"));
        this.formatConfig = loadFormatConfig(new File(plugin.getDataFolder(), "format_config.json"));
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
}
