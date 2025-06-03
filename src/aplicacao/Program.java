package aplicacao;

import entidades.Proprietario;
import entidades.Transferencia;
import entidades.Veiculo;
import servicos.Gerenciador;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Veiculo> carrosList = new ArrayList<>();
        Gerenciador gerenciador = new Gerenciador();
        int n;

        do {
            System.out.println("1 - Cadastro de veículos");
            System.out.println("2 - Transferência de propriedade");
            System.out.println("3 - Consulta de informações");
            System.out.println("4 - Relatórios");
            System.out.println("5 - Sair");
            System.out.print("Escolha uma opção: ");
            n = sc.nextInt();
            sc.nextLine();

            switch (n){

                //CADASTRO DE VEICULOS
                case 1:
                    gerenciador.cadastrarVeiculo();
                    System.out.println();
                    break;

                //TRANSFERENCIA DE PROPRIEDADE
                case 2:
                    System.out.println("Digite os dados da transferencia");
                    System.out.println("Escolha o veículo para fazer a transferencia: ");
                    System.out.println();
                    for (int i = 0; i < carrosList.size(); i++){
                        System.out.println("Código: " + (i+1));
                        System.out.println("Placa: " + carrosList.get(i).getPlaca());
                        System.out.println("Modelo: " + carrosList.get(i).getModelo());
                        System.out.println();
                    }
                    System.out.print("Digite o código do veículo: ");
                    int cod = sc.nextInt() - 1;
                    sc.nextLine();
                    System.out.print("Digite o nome do novo proprietário: ");
                    String nomeNovoProprietario = sc.nextLine();
                    System.out.print("Digite o CPF do novo proprietário: ");
                    String cpfNovoProprietario = sc.next();
                    sc.nextLine();
                    System.out.print("Digite a data de transferência: ");
                    String data = sc.next();
                    LocalDate dataTransferencia = LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy"));


                    Proprietario antigoProprietario = carrosList.get(cod).getProprietarioAtual();
                    Proprietario novoProprietario = new Proprietario(nomeNovoProprietario, cpfNovoProprietario);
                    Transferencia transferencia = new Transferencia(antigoProprietario, novoProprietario, dataTransferencia);
                    carrosList.get(cod).transferirPropriedade(novoProprietario);
                    carrosList.get(cod).adicionarTransferencia(transferencia);
                    System.out.println("Transferência concluída!");
                    System.out.println();
                    break;

                //CONSULTA DE INFORMAÇÕES
                case 3:
                    System.out.println("1 - Consultar veículos por placa");
                    System.out.println("2 - Consultar veículos por proprietários");
                    System.out.println("3 - Consultar histórico de transferências de um veículo");
                    System.out.println("4 - Voltar");
                    System.out.print("O que deseja consultar? ");
                    int numConsulta = sc.nextInt();
                    sc.nextLine();

                    switch (numConsulta){
                        case 1:
                            System.out.print("Digite a placa do veículo: ");
                            String placaForSearch = sc.nextLine();
                            Veiculo encontrado = buscarPorPlaca(carrosList, placaForSearch);

                            if (encontrado != null){
                                System.out.println();
                                System.out.println("Veículo encontrado: ");
                                System.out.println(encontrado);
                            } else {
                                System.out.println();
                                System.out.println("Veículo não encontrado.");
                            }
                            System.out.println();
                            break;

                        case 2:
                            System.out.println("Digite o nome do proprietário: ");
                            System.out.println();
                            break;

                        case 3:
                            System.out.println("Escolha o veículo para consultar seu histórico");
                            System.out.println();
                            for (int i = 0; i < carrosList.size(); i++){
                                System.out.println("Código: " + (i+1));
                                System.out.println(carrosList.get(i));
                            }
                            System.out.println();
                            System.out.print("Digite o código do veículo: ");
                            int cod2 = sc.nextInt();
                            System.out.println("Histórico de transferências: \n");
                            for (Transferencia t : carrosList.get(cod2-1).getHistoricoTransfer()){
                                System.out.println(t);
                            }
                            System.out.println();
                            break;

                        case 4:
                            break;

                        default:
                            System.out.println("OPÇÃO INVÁLIDA!");
                            break;
                    }

                    System.out.println();
                    break;

                //RELATÓRIOS
                case 4:
                    System.out.println("1 - Quantidade de veículos por marca");
                    System.out.println("2 - Veículos transferidos em determinado período");
                    System.out.println("3 - Veículos com placa antiga ainda não transferidos");
                    break;

                case 5:
                    System.out.println("Saindo...");
                    break;

                default:
                    System.out.println("OPÇÃO INVÁLIDA! Tente novamente.");
                    System.out.println();
                    break;
            }

        } while (n != 5);



        sc.close();
    }

    public static Veiculo buscarPorPlaca(List<Veiculo> lista, String placaForSearch){
        for (Veiculo v : lista){
            if (v.getPlaca().equalsIgnoreCase(placaForSearch)){
                return v;
            }
        }
        return null;
    }
}
