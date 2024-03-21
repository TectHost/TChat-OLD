package minealex.tchat.blocked;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;

public class Replacer implements Listener {

    private static Replacer instance;
    private final FileConfiguration config;
    private boolean replacerEnabled;

    private Replacer(FileConfiguration config) {
        this.config = config;
        this.replacerEnabled = config.getBoolean("replacer_enabled", true);
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (!replacerEnabled) return;
        
        Player player = event.getPlayer();
        List<String> msg = Arrays.asList(event.getMessage().split(" "));
        for (String s : config.getConfigurationSection("words").getKeys(false)) {
            String original = config.getString("words." + s + ".original");
            String replacer_text = config.getString("words." + s + ".replace");
            boolean permissionRequired = config.getBoolean("words." + s + ".permission-required", false);
            String permission = "tchat.replacer." + s;

            // Check permission requirement
            if (permissionRequired && !player.hasPermission(permission))
                continue;

            if (msg.contains(original)) {
                int index = msg.indexOf(original);
                msg.set(index, replacer_text);
            }
        }
        event.setMessage(String.join(" ", msg));
    }
    
    public void setReplacerEnabled(boolean enabled) {
        this.replacerEnabled = enabled;
    }

    public boolean isReplacerEnabled() {
        return replacerEnabled;
    }

    public static Replacer getInstance(FileConfiguration config) {
        if (instance == null) {
            instance = new Replacer(config);
        }
        return instance;
    }
}
