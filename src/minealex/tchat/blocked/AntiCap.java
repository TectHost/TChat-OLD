package minealex.tchat.blocked;

public class AntiCap {
    public static String fixCaps(String message) {
        // Dividir el mensaje en palabras
        String[] words = message.split(" ");

        // Crear un StringBuilder para construir el mensaje corregido
        StringBuilder correctedMessage = new StringBuilder();

        // Iterar a través de las palabras del mensaje
        for (String word : words) {
            // Verificar si la palabra tiene más de una letra
            if (word.length() > 1) {
                // Convertir la primera letra a mayúscula y el resto a minúscula
                String correctedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();

                // Agregar la palabra corregida al mensaje corregido
                correctedMessage.append(correctedWord).append(" ");
            } else {
                // Si la palabra tiene una sola letra, agregarla como está
                correctedMessage.append(word).append(" ");
            }
        }

        // Eliminar el espacio final y devolver el mensaje corregido
        return correctedMessage.toString().trim();
    }
}
