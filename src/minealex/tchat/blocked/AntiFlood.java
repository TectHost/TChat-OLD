package minealex.tchat.blocked;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiFlood {
    private Map<UUID, Long> lastMessageTime;
    private int chatCooldownSeconds;

    public AntiFlood(int chatCooldownSeconds) {
        this.chatCooldownSeconds = chatCooldownSeconds;
        this.lastMessageTime = new HashMap<>();
    }

    public boolean canPlayerChat(Player player) {
        UUID playerId = player.getUniqueId();

        if (lastMessageTime.containsKey(playerId)) {
            long lastMessageTimestamp = lastMessageTime.get(playerId);
            long currentTime = System.currentTimeMillis();
            long elapsedTime = (currentTime - lastMessageTimestamp) / 1000;

            if (elapsedTime < chatCooldownSeconds) {
                return false;
            }
        }

        lastMessageTime.put(playerId, System.currentTimeMillis());
        return true;
    }

    public int getRemainingTime(Player player) {
        UUID playerId = player.getUniqueId();

        if (lastMessageTime.containsKey(playerId)) {
            long lastMessageTimestamp = lastMessageTime.get(playerId);
            long currentTime = System.currentTimeMillis();
            long elapsedTime = (currentTime - lastMessageTimestamp) / 1000;

            if (elapsedTime < chatCooldownSeconds) {
                return (int) (chatCooldownSeconds - elapsedTime);
            }
        }

        return 0; // No queda tiempo de espera
    }
}
