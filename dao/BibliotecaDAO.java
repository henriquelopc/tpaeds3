package dao;
import model.Biblioteca;
import dao.ClienteDAO;
import model.Cliente;
import java.util.List;
import java.util.ArrayList;

public class BibliotecaDAO {
    private Arquivo<Biblioteca> arq;
    private ArvoreBMaisClienteBiblioteca indiceCliente; // NOVA LINHA
    
    public BibliotecaDAO() throws Exception {
        arq = new Arquivo<>("bibliotecas", Biblioteca.class.getConstructor());
        indiceCliente = new ArvoreBMaisClienteBiblioteca(4); // NOVA LINHA
        carregarIndiceCliente(); // NOVA LINHA
    }
    
    // NOVO MÉTODO - Carregar bibliotecas existentes no índice
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
    
    // NOVO MÉTODO AUXILIAR - Listar todas as bibliotecas
    private List<Biblioteca> listarTodas() throws Exception {
        List<Biblioteca> bibliotecas = new ArrayList<Biblioteca>();
        
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
    
    // MÉTODOS ORIGINAIS (mantidos iguais)
    public Biblioteca buscar(int id) throws Exception {
        return arq.read(id);
    }
    
    public boolean incluir(Biblioteca b) throws Exception {
        boolean sucesso = arq.create(b) > 0;
        if (sucesso) {
            indiceCliente.inserirBiblioteca(b); // NOVA LINHA
        }
        return sucesso;
    }
    
    public boolean alterar(Biblioteca b) throws Exception {
        // NOVA LÓGICA - Remove versão antiga do índice
        Biblioteca bibliotecaAntiga = buscar(b.getId());
        if (bibliotecaAntiga != null) {
            indiceCliente.removerBiblioteca(bibliotecaAntiga.getClienteId(), bibliotecaAntiga.getId());
        }
        
        boolean sucesso = arq.update(b);
        if (sucesso) {
            indiceCliente.inserirBiblioteca(b); // Adiciona versão atualizada
        }
        return sucesso;
    }
    
    public boolean incluirComValidacao(Biblioteca b) throws Exception {
        if (b.getClienteId() > 0) {
            ClienteDAO cd = new ClienteDAO();
            Cliente cli = cd.buscarCliente(b.getClienteId());
            if (cli == null) {
                System.out.println("Cliente informado não existe: " + b.getClienteId());
                return false;
            }
        }
        return incluir(b); // Chama incluir() que já atualiza o índice
    }
    
    public boolean alterarComValidacao(Biblioteca b) throws Exception {
        if (b.getClienteId() > 0) {
            ClienteDAO cd = new ClienteDAO();
            Cliente cli = cd.buscarCliente(b.getClienteId());
            if (cli == null) {
                System.out.println("Cliente informado não existe: " + b.getClienteId());
                return false;
            }
        }
        return alterar(b); // Chama alterar() que já atualiza o índice
    }
    
    public boolean excluir(int id) throws Exception {
        // NOVA LÓGICA - Remove do índice também
        Biblioteca biblioteca = buscar(id);
        boolean sucesso = arq.delete(id);
        if (sucesso && biblioteca != null) {
            indiceCliente.removerBiblioteca(biblioteca.getClienteId(), biblioteca.getId());
        }
        return sucesso;
    }
    
    // ============ NOVOS MÉTODOS - Busca por relacionamento usando Árvore B+ ============
    
    // Buscar biblioteca de um cliente específico (relacionamento 1:1)
    public Biblioteca buscarBibliotecaDoCliente(int clienteId) {
        return indiceCliente.buscarBibliotecaDoCliente(clienteId);
    }
    
    // Buscar bibliotecas por faixa de clientes (para relatórios)
    public List<Biblioteca> buscarPorFaixaCliente(int clienteIdMin, int clienteIdMax) {
        return indiceCliente.buscarPorFaixaCliente(clienteIdMin, clienteIdMax);
    }
    
    // Listar todas as bibliotecas ordenadas por clienteId
    public List<Biblioteca> listarTodasOrdenadosPorCliente() {
        return indiceCliente.listarTodasOrdenadosPorCliente();
    }
    
    // Verificar se cliente já possui biblioteca
    public boolean clienteJaPossuiBiblioteca(int clienteId) {
        return buscarBibliotecaDoCliente(clienteId) != null;
    }
    
    // Estatísticas usando o índice
    public void exibirEstatisticasCliente() {
        System.out.println("\n=== ESTATISTICAS CLIENTE-BIBLIOTECA ===");
        
        List<Biblioteca> todas = listarTodasOrdenadosPorCliente();
        System.out.println("Total de bibliotecas: " + todas.size());
        
        // Contar clientes únicos
        Set<Integer> clientesUnicos = new HashSet<>();
        for (Biblioteca bib : todas) {
            clientesUnicos.add(bib.getClienteId());
        }
        System.out.println("Clientes com biblioteca: " + clientesUnicos.size());
        
        System.out.println("========================================\n");
    }
    
    // Método para debug da árvore
    public void exibirEstruturaIndice() {
        indiceCliente.exibirEstrutura();
    }
}
