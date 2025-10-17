package dao;

import model.Biblioteca;
import java.util.*;

public class ArvoreBMaisClienteBiblioteca {
    private NoBCliente raiz;
    private int ordem;
    private NoFolhaClienteBib primeiraFolha;
    
    public ArvoreBMaisClienteBiblioteca(int ordem) {
        this.ordem = ordem;
        this.raiz = new NoFolhaClienteBib();
        this.primeiraFolha = (NoFolhaClienteBib) raiz;
    }
    
    // Inserir biblioteca indexada por clienteId
    public void inserirBiblioteca(Biblioteca biblioteca) {
        inserir(biblioteca.getClienteId(), biblioteca);
    }
    
    private void inserir(int clienteId, Biblioteca biblioteca) {
        NoFolhaClienteBib folha = encontrarFolha(clienteId);
        folha.inserir(clienteId, biblioteca, ordem);
        
        if (folha.isFull(ordem)) {
            dividirFolha(folha);
        }
    }
    
    // BUSCA PRINCIPAL: biblioteca de um cliente específico
    public Biblioteca buscarBibliotecaDoCliente(int clienteId) {
        NoFolhaClienteBib folha = encontrarFolha(clienteId);
        List<Biblioteca> bibliotecas = folha.buscarBibliotecasPorCliente(clienteId);
        
        // Como é 1:1, retorna a primeira (deveria ser única)
        return bibliotecas.isEmpty() ? null : bibliotecas.get(0);
    }
    
    // Buscar bibliotecas por faixa de clientes (para relatórios)
    public List<Biblioteca> buscarPorFaixaCliente(int clienteIdMin, int clienteIdMax) {
        List<Biblioteca> resultado = new ArrayList<Biblioteca>();
        NoFolhaClienteBib folhaAtual = encontrarFolha(clienteIdMin);
        
        while (folhaAtual != null) {
            for (int i = 0; i < folhaAtual.chaves.size(); i++) {
                int clienteId = folhaAtual.chaves.get(i);
                
                if (clienteId >= clienteIdMin && clienteId <= clienteIdMax) {
                    resultado.addAll(folhaAtual.getValores().get(i));
                } else if (clienteId > clienteIdMax) {
                    return resultado;
                }
            }
            folhaAtual = folhaAtual.getProximo();
        }
        
        return resultado;
    }
    
    // Listar todas as bibliotecas ordenadas por clienteId
    public List<Biblioteca> listarTodasOrdenadosPorCliente() {
        List<Biblioteca> resultado = new ArrayList<Biblioteca>();
        NoFolhaClienteBib folhaAtual = primeiraFolha;
        
        while (folhaAtual != null) {
            for (List<Biblioteca> bibliotecasComMesmoCliente : folhaAtual.getValores()) {
                resultado.addAll(bibliotecasComMesmoCliente);
            }
            folhaAtual = folhaAtual.getProximo();
        }
        
        return resultado;
    }
    
    // Remover biblioteca
    public boolean removerBiblioteca(int clienteId, int bibliotecaId) {
        NoFolhaClienteBib folha = encontrarFolha(clienteId);
        List<Biblioteca> bibliotecas = folha.buscarBibliotecasPorCliente(clienteId);
        
        boolean removido = false;
        for (int i = 0; i < bibliotecas.size(); i++) {
            if (bibliotecas.get(i).getId() == bibliotecaId) {
                bibliotecas.remove(i);
                removido = true;
                break;
            }
        }
        
        // Se lista ficou vazia, remove o clienteId
        if (removido && bibliotecas.isEmpty()) {
            int pos = folha.encontrarPosicao(clienteId);
            if (pos < folha.chaves.size() && folha.chaves.get(pos) == clienteId) {
                folha.chaves.remove(pos);
                folha.getValores().remove(pos);
            }
        }
        
        return removido;
    }
    
    // Encontrar folha que deveria conter o clienteId
    private NoFolhaClienteBib encontrarFolha(int clienteId) {
        NoBCliente atual = raiz;
        
        while (!atual.ehFolha) {
            NoInternoClienteBib noInterno = (NoInternoClienteBib) atual;
            int pos = 0;
            
            while (pos < atual.chaves.size() && clienteId >= atual.chaves.get(pos)) {
                pos++;
            }
            
            atual = noInterno.getFilhos().get(pos);
        }
        
        return (NoFolhaClienteBib) atual;
    }
    
    // Dividir folha quando fica cheia
    private void dividirFolha(NoFolhaClienteBib folha) {
        NoFolhaClienteBib novaFolha = new NoFolhaClienteBib();
        int meio = ordem / 2;
        
        // Move metade das chaves para nova folha
        while (folha.chaves.size() > meio) {
            int ultimoIndice = folha.chaves.size() - 1;
            novaFolha.chaves.add(0, folha.chaves.remove(ultimoIndice));
            novaFolha.getValores().add(0, folha.getValores().remove(ultimoIndice));
        }
        
        // Atualiza links entre folhas
        novaFolha.setProximo(folha.getProximo());
        novaFolha.setAnterior(folha);
        
        if (folha.getProximo() != null) {
            folha.getProximo().setAnterior(novaFolha);
        }
        folha.setProximo(novaFolha);
        
        // Promove chave para pai
        int chavePromovida = novaFolha.chaves.get(0);
        
        if (folha.pai == null) {
            // Criar nova raiz
            NoInternoClienteBib novaRaiz = new NoInternoClienteBib();
            novaRaiz.chaves.add(chavePromovida);
            novaRaiz.getFilhos().add(folha);
            novaRaiz.getFilhos().add(novaFolha);
            
            folha.pai = novaRaiz;
            novaFolha.pai = novaRaiz;
            raiz = novaRaiz;
        } else {
            NoInternoClienteBib pai = (NoInternoClienteBib) folha.pai;
            pai.inserirChave(chavePromovida, novaFolha);
        }
    }
    
    // Método para debug
    public void exibirEstrutura() {
        System.out.println("\n=== Estrutura da Árvore B+ (Cliente-Biblioteca) ===");
        NoFolhaClienteBib folhaAtual = primeiraFolha;
        int folhaNum = 1;
        
        while (folhaAtual != null) {
            System.out.println("Folha " + folhaNum + ":");
            for (int i = 0; i < folhaAtual.chaves.size(); i++) {
                int clienteId = folhaAtual.chaves.get(i);
                int qtdBibliotecas = folhaAtual.getValores().get(i).size();
                System.out.println("  Cliente " + clienteId + " -> " + qtdBibliotecas + " biblioteca(s)");
            }
            folhaAtual = folhaAtual.getProximo();
            folhaNum++;
        }
        System.out.println("================================================\n");
    }
}

// ============ CLASSES DOS NÓS ============

abstract class NoBCliente {
    protected List<Integer> chaves; // clienteId
    protected boolean ehFolha;
    protected NoBCliente pai;
    
    public NoBCliente(boolean ehFolha) {
        this.chaves = new ArrayList<Integer>();
        this.ehFolha = ehFolha;
        this.pai = null;
    }
    
    public abstract boolean isFull(int ordem);
    public abstract void inserir(int clienteId, Biblioteca biblioteca, int ordem);
}

class NoInternoClienteBib extends NoBCliente {
    private List<NoBCliente> filhos;
    
    public NoInternoClienteBib() {
        super(false);
        this.filhos = new ArrayList<NoBCliente>();
    }
    
    @Override
    public boolean isFull(int ordem) {
        return chaves.size() >= ordem - 1;
    }
    
    @Override
    public void inserir(int clienteId, Biblioteca biblioteca, int ordem) {
        int pos = 0;
        while (pos < chaves.size() && clienteId > chaves.get(pos)) {
            pos++;
        }
        filhos.get(pos).inserir(clienteId, biblioteca, ordem);
    }
    
    public void inserirChave(int chave, NoBCliente novoFilho) {
        int pos = 0;
        while (pos < chaves.size() && chave > chaves.get(pos)) {
            pos++;
        }
        chaves.add(pos, chave);
        filhos.add(pos + 1, novoFilho);
        novoFilho.pai = this;
    }
    
    public List<NoBCliente> getFilhos() { 
        return filhos; 
    }
}

class NoFolhaClienteBib extends NoBCliente {
    private List<List<Biblioteca>> valores;
    private NoFolhaClienteBib proximo;
    private NoFolhaClienteBib anterior;
    
    public NoFolhaClienteBib() {
        super(true);
        this.valores = new ArrayList<List<Biblioteca>>();
        this.proximo = null;
        this.anterior = null;
    }
    
    @Override
    public boolean isFull(int ordem) {
        return chaves.size() >= ordem;
    }
    
    @Override
    public void inserir(int clienteId, Biblioteca biblioteca, int ordem) {
        int pos = encontrarPosicao(clienteId);
        
        if (pos < chaves.size() && chaves.get(pos) == clienteId) {
            // Cliente já existe, adiciona biblioteca
            valores.get(pos).add(biblioteca);
        } else {
            // Novo cliente
            chaves.add(pos, clienteId);
            List<Biblioteca> listaBibliotecas = new ArrayList<Biblioteca>();
            listaBibliotecas.add(biblioteca);
            valores.add(pos, listaBibliotecas);
        }
    }
    
    public int encontrarPosicao(int clienteId) {
        int pos = 0;
        while (pos < chaves.size() && clienteId > chaves.get(pos)) {
            pos++;
        }
        return pos;
    }
    
    public List<Biblioteca> buscarBibliotecasPorCliente(int clienteId) {
        int pos = encontrarPosicao(clienteId);
        if (pos < chaves.size() && chaves.get(pos) == clienteId) {
            return new ArrayList<Biblioteca>(valores.get(pos));
        }
        return new ArrayList<Biblioteca>();
    }
    
    // Getters e Setters
    public List<List<Biblioteca>> getValores() { return valores; }
    public NoFolhaClienteBib getProximo() { return proximo; }
    public NoFolhaClienteBib getAnterior() { return anterior; }
    public void setProximo(NoFolhaClienteBib proximo) { this.proximo = proximo; }
    public void setAnterior(NoFolhaClienteBib anterior) { this.anterior = anterior; }
}
