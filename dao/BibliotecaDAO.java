package dao;
import model.Biblioteca;
import dao.ClienteDAO;
import model.Cliente;
import java.util.List;
import java.util.ArrayList;

public class BibliotecaDAO {
    private Arquivo<Biblioteca> arq;
    private ArvoreBMaisClienteBiblioteca indiceCliente; // ÁRVORE B+ APENAS
    
    public BibliotecaDAO() throws Exception {
        arq = new Arquivo<>("bibliotecas", Biblioteca.class.getConstructor());
        indiceCliente = new ArvoreBMaisClienteBiblioteca(4); // ÁRVORE B+ ordem 4
        carregarIndiceCliente();
    }
    
    // Carregar bibliotecas existentes na Árvore B+
    private void carregarIndiceCliente() {
        try {
            List<Biblioteca> bibliotecas = listarTodas();
            for (Biblioteca biblioteca : bibliotecas) {
                indiceCliente.inserirBiblioteca(biblioteca);
            }
            System.out.println("Indice B+ cliente-biblioteca carregado com " + bibliotecas.size() + " bibliotecas");
        } catch (Exception e) {
            System.err.println("Erro ao carregar indice: " + e.getMessage());
        }
    }
    
    // Método auxiliar para listar todas as bibliotecas
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
    
    // ============ MÉTODOS ORIGINAIS (mantidos iguais) ============
    
    public Biblioteca buscar(int id) throws Exception {
        return arq.read(id);
    }
    
    public boolean incluir(Biblioteca b) throws Exception {
        boolean sucesso = arq.create(b) > 0;
        if (sucesso) {
            indiceCliente.inserirBiblioteca(b); // Atualiza Árvore B+
        }
        return sucesso;
    }
    
    public boolean alterar(Biblioteca b) throws Exception {
        // Remove versão antiga da Árvore B+
        Biblioteca bibliotecaAntiga = buscar(b.getId());
        if (bibliotecaAntiga != null) {
            indiceCliente.removerBiblioteca(bibliotecaAntiga.getClienteId(), bibliotecaAntiga.getId());
        }
        
        boolean sucesso = arq.update(b);
        if (sucesso) {
            indiceCliente.inserirBiblioteca(b); // Adiciona versão nova na Árvore B+
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
        return incluir(b); // Usa incluir() que já atualiza a Árvore B+
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
        return alterar(b); // Usa alterar() que já atualiza a Árvore B+
    }
    
    public boolean excluir(int id) throws Exception {
        // Remove da Árvore B+ também
        Biblioteca biblioteca = buscar(id);
        boolean sucesso = arq.delete(id);
        if (sucesso && biblioteca != null) {
            indiceCliente.removerBiblioteca(biblioteca.getClienteId(), biblioteca.getId());
        }
        return sucesso;
    }
    
    // ============ NOVOS MÉTODOS - Busca usando Árvore B+ ============
    
    // PRINCIPAL: buscar biblioteca de um cliente específico
    public Biblioteca buscarBibliotecaDoCliente(int clienteId) {
        return indiceCliente.buscarBibliotecaDoCliente(clienteId);
    }
    
    // Buscar bibliotecas de uma faixa de clientes (ordenado)
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
    
    // Exibir estrutura da Árvore B+ (para debug)
    public void exibirEstruturaIndice() {
        indiceCliente.exibirEstrutura();
    }
}
