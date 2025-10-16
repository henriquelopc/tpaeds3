package dao;
import model.Jogo;
import java.util.List;
import java.util.ArrayList;

public class JogoDAO {
    private Arquivo<Jogo> arq;
    private IndicePrecoJogo indicePreco; 
    
    public JogoDAO() throws Exception {
        System.out.println("  -> Inicio do construtor JogoDAO");
        
        System.out.println("  -> Criando Arquivo...");
        arq = new Arquivo<>("jogos", Jogo.class.getConstructor());
        System.out.println("  -> Arquivo criado com sucesso!");
        
        System.out.println("  -> Criando IndicePrecoJogo...");
        indicePreco = new IndicePrecoJogo();
        System.out.println("  -> IndicePrecoJogo criado!");
        
        System.out.println("  -> Iniciando carregamento do indice...");
        carregarIndicePreco();
        System.out.println("  -> Indice carregado!");
        
        System.out.println("  -> JogoDAO totalmente inicializado!");
    }
    
    private void carregarIndicePreco() {
        try {
            System.out.println("    -> Chamando listarTodos()...");
            List<Jogo> jogos = listarTodos();
            System.out.println("    -> listarTodos() retornou " + jogos.size() + " jogos");
            
            System.out.println("    -> Adicionando jogos ao indice...");
            for (int i = 0; i < jogos.size(); i++) {
                Jogo jogo = jogos.get(i);
                System.out.println("      -> Adicionando jogo " + (i+1) + ": " + jogo.getNome());
                indicePreco.adicionarJogo(jogo);
            }
            System.out.println("    -> Todos os jogos adicionados ao indice");
            
        } catch (Exception e) {
            System.err.println("    -> ERRO em carregarIndicePreco: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private List<Jogo> listarTodos() throws Exception {
        System.out.println("      -> Inicio do listarTodos()");
        List<Jogo> jogos = new ArrayList<Jogo>();
        
        int id = 1;
        int tentativas = 0;
        int maxTentativas = 100; // LIMITE para evitar loop infinito
        
        while (tentativas < maxTentativas) {
            try {
                System.out.println("        -> Tentando ler ID: " + id + " (tentativa " + (tentativas + 1) + ")");
                Jogo jogo = arq.read(id);
                
                if (jogo != null) {
                    System.out.println("        -> Jogo encontrado: " + jogo.getNome());
                    jogos.add(jogo);
                } else {
                    System.out.println("        -> Jogo ID " + id + " e null");
                }
                
                id++;
                tentativas++;
                
            } catch (Exception e) {
                System.out.println("        -> Excecao para ID " + id + ": " + e.getMessage());
                tentativas++;
                
                // Se teve muitas exceções seguidas, para
                if (tentativas > 10) {
                    System.out.println("        -> Muitas excecoes, parando busca");
                    break;
                }
                
                id++;
            }
        }
        
        System.out.println("      -> Fim do listarTodos(), encontrados: " + jogos.size() + " jogos");
        return jogos;
    }
    
    // MÉTODOS ORIGINAIS
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
        Jogo jogoAntigo = buscar(j.getId());
        if (jogoAntigo != null) {
            indicePreco.removerJogo(jogoAntigo);
        }
        
        boolean sucesso = arq.update(j);
        if (sucesso) {
            indicePreco.adicionarJogo(j);
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
    
    // NOVOS MÉTODOS - Buscas por preço
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
        System.out.println("\n=== ESTATISTICAS DE PRECOS ===");
        
        List<Jogo> gratuitos = buscarPorPreco(0.0);
        List<Jogo> baratos = buscarPorFaixaPreco(0.01, 29.99);
        List<Jogo> medios = buscarPorFaixaPreco(30.0, 79.99);
        List<Jogo> caros = buscarPorFaixaPreco(80.0, 149.99);
        List<Jogo> premium = buscarJogosPremium();
        
        System.out.println("Gratuitos (R$ 0,00): " + gratuitos.size() + " jogos");
        System.out.println("Baratos (R$ 0,01 - 29,99): " + baratos.size() + " jogos");
        System.out.println("Medios (R$ 30,00 - 79,99): " + medios.size() + " jogos");
        System.out.println("Caros (R$ 80,00 - 149,99): " + caros.size() + " jogos");
        System.out.println("Premium (R$ 150,00+): " + premium.size() + " jogos");
        System.out.println("===============================\n");
    }
}
