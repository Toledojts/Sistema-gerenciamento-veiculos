package utilitarios;

import java.util.regex.Pattern;

public class PlacaUtil {
    private static final Pattern PADRAO_PLACA_ANTIGA_COM_HIFEN = Pattern.compile("^[A-Z]{3}-\\d{4}$");
    private static final Pattern PADRAO_PLACA_ANTIGA_SEM_HIFEN = Pattern.compile("^[A-Z]{3}\\d{4}$");
    private static final Pattern PADRAO_PLACA_MERCOSUL = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$");

    /*
     * Verifica se a placa está no formato antigo (LLL-NNNN ou LLLNNNN).
     * parametro placa -- Placa a ser verificada.
     * retorna true se for formato antigo, false caso contrário.
     */


    public static boolean ehPlacaAntiga(String placa) {
        if (placa == null) return false;
        String placaUpper = placa.toUpperCase();
        return PADRAO_PLACA_ANTIGA_COM_HIFEN.matcher(placaUpper).matches() ||
                PADRAO_PLACA_ANTIGA_SEM_HIFEN.matcher(placaUpper).matches();
    }

    /*
     * Verifica se a placa já está no formato Mercosul (LLLNLNN).
     * parametro placa -- Placa a ser verificada.
     * retorna true se for formato Mercosul, false caso contrário.
     */
    public static boolean ehPlacaMercosul(String placa) {
        if (placa == null) return false;
        return PADRAO_PLACA_MERCOSUL.matcher(placa.toUpperCase()).matches();
    }

    /*
     * Converte uma placa do formato antigo (LLL-NNNN ou LLLNNNN) para o padrão Mercosul (LLLNLNN).
     * A conversão típica altera o 5º caractere (índice 4) da placa antiga
     * (que é o 2º dígito numérico) para uma letra correspondente (0=A, 1=B, ..., 9=J).
     * Se a placa já estiver no formato Mercosul ou não for um formato antigo válido,
     * retorna a placa original.
     */

    public static String converterPlacaAntigaParaMercosul(String placaAntiga) {
        if (placaAntiga == null) return null;
        String placaLimpa = placaAntiga.toUpperCase().replace("-", "");

        if (!PADRAO_PLACA_ANTIGA_SEM_HIFEN.matcher(placaLimpa).matches()) {
            // Não é um formato antigo válido para conversão ou já pode ser Mercosul
            return placaAntiga.toUpperCase(); // Retorna a placa original em maiúsculas
        }

        // LLLNNNN -> LLLNLNN
        // Ex: ABC1234 -> ABC1C34
        char[] letrasConversao = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};

        char l1 = placaLimpa.charAt(0);
        char l2 = placaLimpa.charAt(1);
        char l3 = placaLimpa.charAt(2);
        char n1 = placaLimpa.charAt(3); // Primeiro número
        char n2Original = placaLimpa.charAt(4); // Segundo número (será convertido)
        char n3 = placaLimpa.charAt(5); // Terceiro número
        char n4 = placaLimpa.charAt(6); // Quarto número

        int indiceN2 = Character.getNumericValue(n2Original);
        char lNovaPos5 = letrasConversao[indiceN2]; // Converte o segundo número para letra

        return "" + l1 + l2 + l3 + n1 + lNovaPos5 + n3 + n4;
    }

}
