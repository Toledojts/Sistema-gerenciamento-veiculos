package utilitarios;

import java.util.Random;

public class GeradorPlacaUtil {

    private static final Random random = new Random();

    public static String gerarPlacaMercosul() {
        StringBuilder sb = new StringBuilder();

        // Gera 3 letras (LLL)
        for (int i = 0; i < 3; i++) {
            char letra = (char) (random.nextInt(26) + 'A');
            sb.append(letra);
        }

        // Gera 1 número (N)
        sb.append(random.nextInt(10));

        // Gera 1 letra (L)
        char letraMeio = (char) (random.nextInt(26) + 'A');
        sb.append(letraMeio);

        // Gera 2 números (NN)
        // String.format garante que números menores que 10 tenham um zero à esquerda (ex: 07)
        sb.append(String.format("%02d", random.nextInt(100)));

        return sb.toString();
    }
}
