package servicos;

import entidades.Proprietario;
import entidades.Veiculo;

import java.util.Scanner;

public class Gerenciador {
    Scanner sc = new Scanner(System.in);

    public Gerenciador() {
    }

    public void cadastrarVeiculo(){
        System.out.println("Digite os dados do proprietário");
        System.out.print("Nome: ");
        String nome = sc.nextLine();
        System.out.print("CPF: ");
        String cpf = sc.next();
        Proprietario proper = new Proprietario(nome, cpf);
        System.out.println();
        sc.nextLine();

        System.out.println("Digite os dados do veículo");
        System.out.print("Placa: ");
        String placa = sc.nextLine();
        System.out.print("Marca: ");
        String marca = sc.nextLine();
        System.out.print("Modelo: ");
        String modelo = sc.nextLine();
        System.out.print("Cor: ");
        String cor = sc.nextLine();
        System.out.print("Ano: ");
        int ano = sc.nextInt();

        new Veiculo(placa, marca, modelo, cor, ano, proper);
    }
}
