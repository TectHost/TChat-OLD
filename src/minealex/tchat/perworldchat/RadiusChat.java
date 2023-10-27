package minealex.tchat.perworldchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RadiusChat {
    private WorldsManager worldsManager;

    public RadiusChat(WorldsManager worldsManager) {
        this.worldsManager = worldsManager;
    }
    
    public void sendMessageToNearbyPlayers(Player sender, String message) {
        String senderName = sender.getName();
        List<Player> nearbyPlayers = getNearbyPlayers(sender);

        for (Player recipient : nearbyPlayers) {
            recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', "[" + sender.getWorld().getName() + "]" + "[" + senderName + "] " + message));
        }
    }

    private List<Player> getNearbyPlayers(Player player) {
        List<Player> nearbyPlayers = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && p.getWorld().equals(player.getWorld()) && p.getLocation().distance(player.getLocation()) <= getRadius(player.getWorld().getName())) {
                nearbyPlayers.add(p);
            }
        }

        return nearbyPlayers;
    }

    private int getRadius(String worldName) {
        WorldConfig worldConfig = worldsManager.loadWorldConfig(worldName);
        return worldConfig.getRadiusChat();
    }
}
