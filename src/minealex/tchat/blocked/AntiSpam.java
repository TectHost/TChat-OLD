package minealex.tchat.blocked;

import org.bukkit.entity.Player;

public class AntiSpam {

    public static boolean containsRepeatedLetters(String message) {
        return message.matches(".*(.)\\1{3,}.*"); // Esta expresión regular busca 4 letras o más repetidas.
    }

    public static void handleSpamMessage(Player player, String spamMessage) {
        player.sendMessage("You cannot send messages with repeated letters!");
    }
}
