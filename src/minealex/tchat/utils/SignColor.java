package minealex.tchat.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SignColor implements Listener {

    @SuppressWarnings("unused")
	private final JavaPlugin plugin;

    public SignColor(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        // Verificar si el jugador tiene el permiso tchat.signcolor
        if (player.hasPermission("tchat.signcolor")) {
            for (int i = 0; i < event.getLines().length; i++) {
                String line = event.getLine(i);
                // Reemplazar & con el código de color
                line = ChatColor.translateAlternateColorCodes('&', line);
                event.setLine(i, line);
            }
        }
    }
}
