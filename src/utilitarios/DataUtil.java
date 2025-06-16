package utilitarios;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DataUtil {
    private static final DateTimeFormatter FORMATADOR_BRASILEIRO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /*
     * Converte uma String de data no formato "dd/MM/yyyy" para um objeto LocalDate.
     * parametro dataStr - A String da data.
     * retorna Um objeto LocalDate se a conversão for bem-sucedida, null caso contrário.
     */
    public static LocalDate parseData(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dataStr.trim(), FORMATADOR_BRASILEIRO);
        } catch (DateTimeParseException e) {
            System.err.println("Erro ao parsear data: " + dataStr + ". Formato esperado dd/MM/yyyy. Detalhe: " + e.getMessage());
            return null;
        }
    }

    /*
     * Formata um objeto LocalDate para uma String no formato "dd/MM/yyyy".
     * parametro data - O objeto LocalDate.
     * retorna Uma String da data formatada, ou null se a data de entrada for null.
     */
    public static String formatarData(LocalDate data) {
        if (data == null) {
            return null;
        }
        return data.format(FORMATADOR_BRASILEIRO);
    }
}
