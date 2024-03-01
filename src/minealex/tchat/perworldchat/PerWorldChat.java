package minealex.tchat.perworldchat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PerWorldChat implements Listener {
    private WorldsManager worldsManager;
    private FileConfiguration formatConfig;
    private File configFile;

    public PerWorldChat(Plugin plugin) {
        this.worldsManager = new WorldsManager(new File(plugin.getDataFolder(), "worlds.yml"));
        this.formatConfig = loadFormatConfig(new File(plugin.getDataFolder(), "format_config.yml"));
        this.configFile = new File(plugin.getDataFolder(), "worlds.yml");

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
            String chatDisabledMessage = formatConfig.getString("messages.chatDisabledMessage");
            chatDisabledMessage = ChatColor.translateAlternateColorCodes('&', chatDisabledMessage);
            player.sendMessage(chatDisabledMessage);
            event.setCancelled(true);
            return;
        }

        if (!worldConfig.isPerWorldChat()) {
            return;
        }

        Set<Player> recipients = event.getRecipients();
        Iterator<Player> iterator = recipients.iterator();

        while (iterator.hasNext()) {
            Player recipient = iterator.next();
            if (!recipient.getWorld().getName().equals(player.getWorld().getName())) {
                iterator.remove();
            }
        }
    }

    private FileConfiguration loadFormatConfig(File configFile) {
        return YamlConfiguration.loadConfiguration(configFile);
    }

    private void initConfig() {
        List<WorldConfig> defaultConfigs = new ArrayList<>();
        defaultConfigs.add(new WorldConfig("world", true, false, false, 10));
        defaultConfigs.add(new WorldConfig("world_nether", true, false, false, 10));
        defaultConfigs.add(new WorldConfig("world_the_end", true, false, false, 10));

        for (WorldConfig worldConfig : defaultConfigs) {
            worldsManager.saveWorldConfig(worldConfig);
        }
    }
}