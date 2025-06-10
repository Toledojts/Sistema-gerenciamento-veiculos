package utilitarios;

import java.util.InputMismatchException;

public class CpfUtil {
    public static boolean validar(String cpf) {
        // Limpa a formatação (pontos e hífen) e verifica se o CPF é nulo ou vazio
        String cpfLimpo = cpf != null ? cpf.replaceAll("[.\\-]", "") : null;

        if (cpfLimpo == null || cpfLimpo.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (ex: 111.111.111-11), o que é inválido.
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Cálculo do primeiro dígito verificador
            char dig10 = calcularDigitoVerificador(cpfLimpo.substring(0, 9));

            // Cálculo do segundo dígito verificador
            char dig11 = calcularDigitoVerificador(cpfLimpo.substring(0, 9) + dig10);

            // Verifica se os dígitos calculados são iguais aos dígitos do CPF informado
            return (dig10 == cpfLimpo.charAt(9)) && (dig11 == cpfLimpo.charAt(10));

        } catch (InputMismatchException e) {
            return false;
        }
    }

    private static char calcularDigitoVerificador(String str) {
        int soma = 0;
        // O peso começa em (tamanho da string + 1)
        int peso = str.length() + 1;

        // Loop para multiplicar cada dígito pelo seu peso
        for (int i = 0; i < str.length(); i++) {
            int num = Character.getNumericValue(str.charAt(i));
            soma = soma + (num * peso);
            peso = peso - 1;
        }

        int resto = 11 - (soma % 11);
        if ((resto == 10) || (resto == 11)) {
            return '0';
        } else {
            // Converte o número do dígito para seu caractere correspondente
            return (char) (resto + '0');
        }
    }
}
