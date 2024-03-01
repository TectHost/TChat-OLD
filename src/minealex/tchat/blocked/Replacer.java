package minealex.tchat.blocked;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.Arrays;
import java.util.List;

public class Replacer implements Listener {

    private static Replacer instance;
    private final FileConfiguration config;

    private Replacer(FileConfiguration config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        List<String> msg = Arrays.asList(event.getMessage().split(" "));
        for (String s : config.getConfigurationSection("words").getKeys(false)) {
            String original = config.getString("words." + s + ".original");
            String replacer_text = config.getString("words." + s + ".replacer_text");
            if (msg.indexOf(original) != -1) {
                msg.set(msg.indexOf(original), replacer_text);
            }
        }
        event.setMessage(String.join(" ", msg));
    }

    public static Replacer getInstance(FileConfiguration config) {
        if (instance == null) {
            instance = new Replacer(config);
        }
        return instance;
    }
}
