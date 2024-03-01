package minealex.tchat.blocked;

public class AntiCap {
    public static String fixCaps(String message) {
        // Dividir el mensaje en palabras
        String[] words = message.split(" ");

        // Crear un StringBuilder para construir el mensaje corregido
        StringBuilder correctedMessage = new StringBuilder();

        // Iterar a través de las palabras del mensaje
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            
            // Verificar si la palabra tiene más de una letra
            if (word.length() > 1) {
                // Si es la primera palabra, convertir solo la primera letra a mayúscula
                if (i == 0) {
                    String firstLetter = word.substring(0, 1).toUpperCase();
                    String restOfWord = word.substring(1).toLowerCase();
                    correctedMessage.append(firstLetter).append(restOfWord);
                } else {
                    // Para las demás palabras, mantenerlas en minúsculas
                    correctedMessage.append(word.toLowerCase());
                }
            } else {
                // Si la palabra tiene una sola letra, agregarla como está
                correctedMessage.append(word);
            }
            
            // Agregar un espacio después de cada palabra (excepto la última)
            if (i < words.length - 1) {
                correctedMessage.append(" ");
            }
        }

        // Devolver el mensaje corregido
        return correctedMessage.toString();
    }
}
