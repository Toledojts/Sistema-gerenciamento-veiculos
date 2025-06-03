package aplicacao;

import db.MarcaDAO;
import db.ModeloDAO;
import entidades.Marca;
import entidades.Modelo;

import java.util.List;

public class Teste {
    public static void main(String[] args) {
        System.out.println("--- Testando MarcaDAO ---");
        MarcaDAO marcaDAO = new MarcaDAO();
        List<Marca> todasMarcas = marcaDAO.listarTodas();

        if (!todasMarcas.isEmpty()) {
            System.out.println("Marcas disponíveis:");
            for (int i = 0; i < todasMarcas.size(); i++) {
                System.out.printf("%d. %s (ID: %d)\n", i + 1, todasMarcas.get(i).getNome(), todasMarcas.get(i).getId());
            }

            // Escolhe uma marca para testar ModeloDAO (ex: a primeira da lista)
            Marca marcaEscolhida = todasMarcas.get(1);
            System.out.println("\n--- Testando ModeloDAO para a marca: " + marcaEscolhida.getNome() + " ---");

            ModeloDAO modeloDAO = new ModeloDAO();
            List<Modelo> modelosDaMarca = modeloDAO.listarPorMarca(marcaEscolhida);

            if (!modelosDaMarca.isEmpty()) {
                System.out.println("Modelos disponíveis para " + marcaEscolhida.getNome() + ":");
                for (int i = 0; i < modelosDaMarca.size(); i++) {
                    System.out.printf("%d. %s (ID: %d)\n", i + 1, modelosDaMarca.get(i).getNome(), modelosDaMarca.get(i).getId());
                }
            } else {
                System.out.println("Nenhum modelo encontrado para " + marcaEscolhida.getNome());
            }

        } else {
            System.out.println("Nenhuma marca encontrada no banco de dados.");
        }
    }
}
