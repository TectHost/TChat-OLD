package minealex.tchat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import minealex.tchat.TChat;

public class PlayerMoveListener implements Listener {
    private TChat plugin;

    public PlayerMoveListener(TChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Marcar al jugador como que se ha movido
        plugin.markPlayerAsMoved(event.getPlayer().getUniqueId());
    }
}

