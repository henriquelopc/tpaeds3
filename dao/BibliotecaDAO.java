package dao;
import model.Biblioteca;
import model.BibliotecaJogo;
import model.Cliente;
import java.util.List;
import java.util.ArrayList;

public class BibliotecaDAO {
    private Arquivo<Biblioteca> arq;
    private HashExtensivelClienteBiblioteca indiceCliente; // HASH para Cliente → Biblioteca
    
    public BibliotecaDAO() throws Exception {
        arq = new Arquivo<>("bibliotecas", Biblioteca.class.getConstructor());
        indiceCliente = new HashExtensivelClienteBiblioteca(4);
        carregarIndiceCliente();
    }
    
    private void carregarIndiceCliente() {
        try {
            List<Biblioteca> bibliotecas = listarTodas();
            for (Biblioteca biblioteca : bibliotecas) {
                indiceCliente.inserirBiblioteca(biblioteca);
            }
            System.out.println("Indice cliente-biblioteca carregado com " + bibliotecas.size() + " bibliotecas");
        } catch (Exception e) {
            System.err.println("Erro ao carregar indice: " + e.getMessage());
        }
    }
    
    // MÉTODOS ORIGINAIS
    public Biblioteca buscar(int id) throws Exception {
        return arq.read(id);
    }
    
    public boolean incluir(Biblioteca b) throws Exception {
        boolean sucesso = arq.create(b) > 0;
        if (sucesso) {
            indiceCliente.inserirBiblioteca(b);
        }
        return sucesso;
    }
    
    public boolean alterar(Biblioteca b) throws Exception {
        Biblioteca bibliotecaAntiga = buscar(b.getId());
        if (bibliotecaAntiga != null) {
            indiceCliente.removerBiblioteca(bibliotecaAntiga.getClienteId());
        }
        
        boolean sucesso = arq.update(b);
        if (sucesso) {
            indiceCliente.inserirBiblioteca(b);
        }
        return sucesso;
    }
    
    public boolean excluir(int id) throws Exception {
        Biblioteca biblioteca = buscar(id);
        boolean sucesso = arq.delete(id);
        if (sucesso && biblioteca != null) {
            indiceCliente.removerBiblioteca(biblioteca.getClienteId());
        }
        return sucesso;
    }
    
    // NOVOS MÉTODOS - Busca por relacionamento
    public Biblioteca buscarBibliotecaDoCliente(int clienteId) {
        return indiceCliente.buscarBibliotecaDoCliente(clienteId);
    }
    
    public List<Biblioteca> listarTodasBibliotecas() {
        return indiceCliente.listarTodasBibliotecas();
    }
    
    // Validação com cliente
    public boolean incluirComValidacao(Biblioteca b) throws Exception {
        if (b.getClienteId() > 0) {
            ClienteDAO cd = new ClienteDAO();
            Cliente cli = cd.buscarCliente(b.getClienteId());
            
            if (cli != null) {
                return incluir(b);
            } else {
                System.err.println("Cliente não encontrado para a biblioteca!");
                return false;
            }
        }
        return incluir(b);
    }
    
    // Método auxiliar
    private List<Biblioteca> listarTodas() throws Exception {
        List<Biblioteca> bibliotecas = new ArrayList<>();
        
        int id = 1;
        int falhasConsecutivas = 0;
        
        while (falhasConsecutivas < 10) {
            try {
                Biblioteca biblioteca = arq.read(id);
                if (biblioteca != null) {
                    bibliotecas.add(biblioteca);
                    falhasConsecutivas = 0;
                } else {
                    falhasConsecutivas++;
                }
                id++;
            } catch (Exception e) {
                falhasConsecutivas++;
                id++;
            }
        }
        
        return bibliotecas;
    }
}
