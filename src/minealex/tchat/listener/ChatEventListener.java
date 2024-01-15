package minealex.tchat.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import minealex.tchat.bot.ChatGames;

public class ChatEventListener implements Listener {

    private ChatGames chatGames;

    public ChatEventListener(ChatGames chatGames) {
        this.chatGames = chatGames;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        chatGames.processChat(player, message);
    }
}
