// Adicionar no JogoDAO.java
public class JogoDAO {
    private Arquivo<Jogo> arquivo;
    private IndicePrecoJogo indicePreco; // NOVA LINHA
    
    public JogoDAO() throws Exception {
        arquivo = new Arquivo<>("Jogo", Jogo.class.getConstructor());
        indicePreco = new IndicePrecoJogo(); // NOVA LINHA
        
        // Carregar jogos existentes no índice
        carregarIndicePreco(); // NOVA LINHA
    }
    
    // NOVO MÉTODO - Carregar índice na inicialização
    private void carregarIndicePreco() {
        try {
            List<Jogo> jogos = listar(); // Usa método existente
            for (Jogo jogo : jogos) {
                indicePreco.adicionarJogo(jogo);
            }
            System.out.println("✅ Índice de preços carregado com " + jogos.size() + " jogos");
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao carregar índice de preços: " + e.getMessage());
        }
    }
    
    // Modificar métodos existentes para manter índice atualizado
    @Override
    public int create(Jogo jogo) throws Exception {
        int id = super.create(jogo);
        if (id > 0) {
            jogo.setId(id);
            indicePreco.adicionarJogo(jogo); // NOVA LINHA
        }
        return id;
    }
    
    @Override
    public boolean update(Jogo jogo) throws Exception {
        // Remover do índice antes de atualizar
        Jogo jogoAntigo = read(jogo.getId());
        if (jogoAntigo != null) {
            indicePreco.removerJogo(jogoAntigo);
        }
        
        boolean sucesso = super.update(jogo);
        if (sucesso) {
            indicePreco.adicionarJogo(jogo); // Adicionar versão atualizada
        }
        return sucesso;
    }
    
    @Override
    public boolean delete(int id) throws Exception {
        Jogo jogo = read(id);
        boolean sucesso = super.delete(id);
        if (sucesso && jogo != null) {
            indicePreco.removerJogo(jogo); // NOVA LINHA
        }
        return sucesso;
    }
    
    // NOVOS MÉTODOS - Buscas por preço usando Árvore B+
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
    
    // NOVO MÉTODO - Relatório por faixas de preço
    public void relatorioPorPreco() {
        System.out.println("\n=== RELATÓRIO DE JOGOS POR PREÇO ===");
        
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
        System.out.println("=====================================\n");
    }
}
