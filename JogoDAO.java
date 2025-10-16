package dao;
import model.Jogo;
import java.util.List;
import java.util.ArrayList;

public class JogoDAO {
    private Arquivo<Jogo> arq;
    private IndicePrecoJogo indicePreco;
    
    public JogoDAO() throws Exception {
        arq = new Arquivo<>("jogos", Jogo.class.getConstructor());
        indicePreco = new IndicePrecoJogo();
        carregarIndicePreco();
    }
    
    // MÉTODOS ORIGINAIS (mantidos iguais)
    public Jogo buscar(int id) throws Exception {
        return arq.read(id);
    }
    
    public boolean incluir(Jogo j) throws Exception {
        boolean sucesso = arq.create(j) > 0;
        if (sucesso) {
            indicePreco.adicionarJogo(j);
        }
        return sucesso;
    }
    
    public boolean alterar(Jogo j) throws Exception {
        // Remover versão antiga do índice
        Jogo jogoAntigo = buscar(j.getId());
        if (jogoAntigo != null) {
            indicePreco.removerJogo(jogoAntigo);
        }
        
        boolean sucesso = arq.update(j);
        if (sucesso) {
            indicePreco.adicionarJogo(j); // Adicionar versão atualizada
        }
        return sucesso;
    }
    
    public boolean excluir(int id) throws Exception {
        Jogo jogo = buscar(id);
        boolean sucesso = arq.delete(id);
        if (sucesso && jogo != null) {
            indicePreco.removerJogo(jogo);
        }
        return sucesso;
    }
    
    // Carregar todos os jogos para o índice
    private void carregarIndicePreco() {
        try {
            List<Jogo> jogos = listarTodos();
            for (Jogo jogo : jogos) {
                indicePreco.adicionarJogo(jogo);
            }
            System.out.println("✅ Índice de preços carregado com " + jogos.size() + " jogos");
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao carregar índice de preços: " + e.getMessage());
        }
    }
    
    // Método auxiliar para listar todos os jogos
    private List<Jogo> listarTodos() throws Exception {
        List<Jogo> jogos = new ArrayList<>();
        
        // Assumindo que o arquivo tem IDs sequenciais, começando de 1 VERIFY
        int id = 1;
        while (true) {
            try {
                Jogo jogo = arq.read(id);
                if (jogo != null) {
                    jogos.add(jogo);
                }
                id++;
            } catch (Exception e) {
                // Não encontrou mais jogos, termina a busca
                break;
            }
        }
        return jogos;
    }
    
    //Buscas por preço usando Árvore B+
    public List<Jogo> buscarPorPreco(double preco) {
        return indicePreco.buscarPorPreco(preco);
    }
    
    public List<Jogo> buscarPorFaixaPreco(double precoMin, double precoMax) {
        return indicePreco.buscarPorFaixaPreco(precoMin, precoMax);
    }
    
    public List<Jogo> buscarJogosPromocao() {
        return indicePreco.buscarJogosPromocao();
    }
    
    public List<Jogo> buscarJogosPremium() {
        return indicePreco.buscarJogosPremium();
    }
    
    public List<Jogo> listarOrdenadosPorPreco() {
        return indicePreco.listarOrdenadosPorPreco();
    }
    
    public void exibirEstatisticasPreco() {
        System.out.println("\n=== ESTATÍSTICAS DE PREÇOS ===");
        
        List<Jogo> gratuitos = buscarPorPreco(0.0);
        List<Jogo> baratos = buscarPorFaixaPreco(0.01, 29.99);
        List<Jogo> medios = buscarPorFaixaPreco(30.0, 79.99);
        List<Jogo> caros = buscarPorFaixaPreco(80.0, 149.99);
        List<Jogo> premium = buscarJogosPremium();
        
        System.out.println("Gratuitos (R$ 0,00): " + gratuitos.size() + " jogos");
        System.out.println("Baratos (R$ 0,01 - 29,99): " + baratos.size() + " jogos");
        System.out.println("Médios (R$ 30,00 - 79,99): " + medios.size() + " jogos");
        System.out.println("Caros (R$ 80,00 - 149,99): " + caros.size() + " jogos");
        System.out.println("Premium (R$ 150,00+): " + premium.size() + " jogos");
        System.out.println("===============================\n");
    }
}
