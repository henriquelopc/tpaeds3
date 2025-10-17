package dao;

import model.BibliotecaJogo;
import java.util.*;

public class ArvoreBMaisBibliotecaJogo {
    private NoB raiz;
    private int ordem;
    private NoFolhaBiblioteca primeiraFolha;
    
    public ArvoreBMaisBibliotecaJogo(int ordem) {
        this.ordem = ordem;
        this.raiz = new NoFolhaBiblioteca();
        this.primeiraFolha = (NoFolhaBiblioteca) raiz;
    }
    
    public void inserirItem(BibliotecaJogo item) {
        inserir(item.getBibliotecaId(), item);
    }
    
    private void inserir(int bibliotecaId, BibliotecaJogo item) {
        NoFolhaBiblioteca folha = encontrarFolha(bibliotecaId);
        folha.inserir(bibliotecaId, item, ordem);
        
        if (folha.isFull(ordem)) {
            dividirFolha(folha);
        }
    }
    
    // BUSCA PRINCIPAL: todos os jogos de uma biblioteca
    public List<BibliotecaJogo> buscarJogosDaBiblioteca(int bibliotecaId) {
        NoFolhaBiblioteca folha = encontrarFolha(bibliotecaId);
        return folha.buscarJogosPorBiblioteca(bibliotecaId);
    }
    
    // Buscar por faixa de bibliotecas (ordenação)
    public List<BibliotecaJogo> buscarPorFaixaBiblioteca(int bibliotecaIdMin, int bibliotecaIdMax) {
        List<BibliotecaJogo> resultado = new ArrayList<>();
        NoFolhaBiblioteca folhaAtual = encontrarFolha(bibliotecaIdMin);
        
        while (folhaAtual != null) {
            for (int i = 0; i < folhaAtual.chaves.size(); i++) {
                int bibliotecaId = folhaAtual.chaves.get(i);
                
                if (bibliotecaId >= bibliotecaIdMin && bibliotecaId <= bibliotecaIdMax) {
                    resultado.addAll(folhaAtual.getValores().get(i));
                } else if (bibliotecaId > bibliotecaIdMax) {
                    return resultado;
                }
            }
            folhaAtual = folhaAtual.getProximo();
        }
        
        return resultado;
    }
    
    // Listar todas as bibliotecas ordenadas por ID
    public List<BibliotecaJogo> listarTodosOrdenadosPorBiblioteca() {
        List<BibliotecaJogo> resultado = new ArrayList<>();
        NoFolhaBiblioteca folhaAtual = primeiraFolha;
        
        while (folhaAtual != null) {
            for (List<BibliotecaJogo> itensComMesmaBiblioteca : folhaAtual.getValores()) {
                resultado.addAll(itensComMesmaBiblioteca);
            }
            folhaAtual = folhaAtual.getProximo();
        }
        
        return resultado;
    }
    
    public boolean removerItem(int bibliotecaId, int jogoId) {
        NoFolhaBiblioteca folha = encontrarFolha(bibliotecaId);
        List<BibliotecaJogo> itens = folha.buscarJogosPorBiblioteca(bibliotecaId);
        
        boolean removido = false;
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).getJogoId() == jogoId) {
                itens.remove(i);
                removido = true;
                break;
            }
        }
        
        if (removido && itens.isEmpty()) {
            int pos = folha.encontrarPosicao(bibliotecaId);
            if (pos < folha.chaves.size() && folha.chaves.get(pos) == bibliotecaId) {
                folha.chaves.remove(pos);
                folha.getValores().remove(pos);
            }
        }
        
        return removido;
    }
    
    private NoFolhaBiblioteca encontrarFolha(int bibliotecaId) {
        NoB atual = raiz;
        
        while (!atual.ehFolha) {
            NoInternoBiblioteca noInterno = (NoInternoBiblioteca) atual;
            int pos = 0;
            
            while (pos < atual.chaves.size() && bibliotecaId >= atual.chaves.get(pos)) {
                pos++;
            }
            
            atual = noInterno.getFilhos().get(pos);
        }
        
        return (NoFolhaBiblioteca) atual;
    }
    
    private void dividirFolha(NoFolhaBiblioteca folha) {
        // Implementação similar à ArvoreBMaisPreco
        // Adaptada para trabalhar com bibliotecaId
    }
}

// Classes auxiliares para nós da árvore B+
class NoInternoBiblioteca extends NoB {
    private List<NoB> filhos;
    
    public NoInternoBiblioteca() {
        super(false);
        this.filhos = new ArrayList<>();
    }
    
    @Override
    public boolean isFull(int ordem) {
        return chaves.size() >= ordem - 1;
    }
    
    @Override
    public void inserir(double chave, Object item, int ordem) {
        // Implementação para nó interno
    }
    
    public List<NoB> getFilhos() { return filhos; }
}

class NoFolhaBiblioteca extends NoB {
    private List<List<BibliotecaJogo>> valores;
    private NoFolhaBiblioteca proximo;
    private NoFolhaBiblioteca anterior;
    
    public NoFolhaBiblioteca() {
        super(true);
        this.valores = new ArrayList<>();
        this.proximo = null;
        this.anterior = null;
    }
    
    @Override
    public boolean isFull(int ordem) {
        return chaves.size() >= ordem;
    }
    
    @Override
    public void inserir(double bibliotecaId, Object item, int ordem) {
        int id = (int) bibliotecaId;
        BibliotecaJogo bibItem = (BibliotecaJogo) item;
        
        int pos = encontrarPosicao(id);
        
        if (pos < chaves.size() && chaves.get(pos).intValue() == id) {
            valores.get(pos).add(bibItem);
        } else {
            chaves.add(pos, (double) id);
            List<BibliotecaJogo> listaItens = new ArrayList<>();
            listaItens.add(bibItem);
            valores.add(pos, listaItens);
        }
    }
    
    public int encontrarPosicao(int bibliotecaId) {
        int pos = 0;
        while (pos < chaves.size() && bibliotecaId > chaves.get(pos)) {
            pos++;
        }
        return pos;
    }
    
    public List<BibliotecaJogo> buscarJogosPorBiblioteca(int bibliotecaId) {
        int pos = encontrarPosicao(bibliotecaId);
        if (pos < chaves.size() && chaves.get(pos).intValue() == bibliotecaId) {
            return new ArrayList<>(valores.get(pos));
        }
        return new ArrayList<>();
    }
    
    public List<List<BibliotecaJogo>> getValores() { return valores; }
    public NoFolhaBiblioteca getProximo() { return proximo; }
    public NoFolhaBiblioteca getAnterior() { return anterior; }
    public void setProximo(NoFolhaBiblioteca proximo) { this.proximo = proximo; }
    public void setAnterior(NoFolhaBiblioteca anterior) { this.anterior = anterior; }
}
