package minealex.tchat.blocked;

import org.bukkit.entity.Player;

public class AntiSpam {

    public static boolean containsRepeatedLetters(String message) {
        return message.matches(".*(.)\\1{3,}.*"); // Esta expresión regular busca 4 letras o más repetidas.
    }

    public static void handleSpamMessage(Player player, String spamMessage) {
        // Aquí puedes implementar lo que desees hacer cuando se detecta un spam.
        // Por ejemplo, enviar un mensaje al jugador y cancelar el evento de chat.
        player.sendMessage("¡No puedes enviar mensajes con letras repetidas!");
    }
}
