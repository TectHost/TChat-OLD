package minealex.tchat.blocked;

import org.bukkit.entity.Player;

public class AntiUnicode {
    private boolean isUnicodeBlocked;

    public AntiUnicode(boolean isUnicodeBlocked) {
        this.isUnicodeBlocked = isUnicodeBlocked;
    }

    public boolean isUnicodeBlocked() {
        return isUnicodeBlocked;
    }

    public boolean canSendUnicode(Player player) {
        if (isUnicodeBlocked) {
            // Verificar si el jugador tiene el permiso para omitir el bloqueo de unicode
            return player.hasPermission("tchat.bypass.unicode");
        }

        // Si el bloqueo de unicode está desactivado, permitir siempre el envío de unicode
        return true;
    }
}
