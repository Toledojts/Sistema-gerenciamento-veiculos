package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static String url = "jdbc:mysql://localhost:3306/banco_psc";
    private static String usuario = "root";
    private static String senha = "caualindao1234";

    static {
        try {
            // Certifique-se de que o driver JDBC do MySQL (mysql-connector-java) está no classpath
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Erro: Driver JDBC do MySQL não encontrado! Verifique o classpath.");
            // Em uma aplicação real, tratar isso de forma mais robusta (lançar exceção, logar)
            throw new RuntimeException("Driver MySQL não encontrado", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("[Conexao] Tentando obter conexão com o banco de dados...");
        Connection conn = DriverManager.getConnection(url, usuario, senha);
        System.out.println("[Conexao] Conexão estabelecida com sucesso.");
        return conn;
    }
}
