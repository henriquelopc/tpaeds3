package teste;

import dao.BibliotecaDAO;
import model.Biblioteca;
import java.util.List;

public class TestRelacionamentoBiblioteca {
    public static void main(String[] args) {
        try {
            BibliotecaDAO bibliotecaDAO = new BibliotecaDAO();
            
            System.out.println("=== TESTE RELACIONAMENTO BIBLIOTECA ===");
            
            // Teste 1: Buscar biblioteca de um cliente específico
            System.out.println("\n1. Buscar biblioteca do cliente 1:");
            Biblioteca bibCliente1 = bibliotecaDAO.buscarBibliotecaDoCliente(1);
            if (bibCliente1 != null) {
                System.out.println("Biblioteca encontrada: ID=" + bibCliente1.getId() + 
                                 ", Cliente=" + bibCliente1.getClienteId());
            } else {
                System.out.println("Cliente 1 nao possui biblioteca");
            }
            
            // Teste 2: Listar todas as bibliotecas
            System.out.println("\n2. Listar todas as bibliotecas:");
            List<Biblioteca> todasBibliotecas = bibliotecaDAO.listarTodasBibliotecas();
            System.out.println("Total de bibliotecas: " + todasBibliotecas.size());
            for (Biblioteca bib : todasBibliotecas) {
                System.out.println("- Biblioteca ID=" + bib.getId() + 
                                 ", Cliente=" + bib.getClienteId());
            }
            
            System.out.println("\n=== TESTE CONCLUIDO ===");
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
