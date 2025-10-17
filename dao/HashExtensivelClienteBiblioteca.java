package dao;

import model.Biblioteca;
import java.util.*;

public class HashExtensivelClienteBiblioteca {
    private List<BucketBiblioteca> diretorio;
    private int profundidadeGlobal;
    private int capacidadeBucket;
    
    public HashExtensivelClienteBiblioteca(int capacidadeBucket) {
        this.profundidadeGlobal = 1;
        this.capacidadeBucket = capacidadeBucket;
        this.diretorio = new ArrayList<>();
        
        BucketBiblioteca bucket1 = new BucketBiblioteca(1, capacidadeBucket);
        BucketBiblioteca bucket2 = new BucketBiblioteca(1, capacidadeBucket);
        
        diretorio.add(bucket1);
        diretorio.add(bucket2);
    }
    
    private int hash(int clienteId) {
        return clienteId % (int)Math.pow(2, profundidadeGlobal);
    }
    
    public void inserirBiblioteca(Biblioteca biblioteca) {
        inserir(biblioteca.getClienteId(), biblioteca);
    }
    
    private void inserir(int clienteId, Biblioteca biblioteca) {
        int indice = hash(clienteId);
        BucketBiblioteca bucket = diretorio.get(indice);
        
        if (!bucket.isFull()) {
            bucket.adicionarBiblioteca(biblioteca);
        } else {
            split(indice);
            inserir(clienteId, biblioteca);
        }
    }
    
    // BUSCA PRINCIPAL: biblioteca de um cliente específico
    public Biblioteca buscarBibliotecaDoCliente(int clienteId) {
        int indice = hash(clienteId);
        BucketBiblioteca bucket = diretorio.get(indice);
        return bucket.buscarBibliotecaPorCliente(clienteId);
    }
    
    // Buscar todas as bibliotecas (para relatórios)
    public List<Biblioteca> listarTodasBibliotecas() {
        List<Biblioteca> resultado = new ArrayList<>();
        Set<BucketBiblioteca> bucketsVisitados = new HashSet<>();
        
        for (BucketBiblioteca bucket : diretorio) {
            if (!bucketsVisitados.contains(bucket)) {
                bucketsVisitados.add(bucket);
                resultado.addAll(bucket.getBibliotecas());
            }
        }
        return resultado;
    }
    
    public boolean removerBiblioteca(int clienteId) {
        int indice = hash(clienteId);
        BucketBiblioteca bucket = diretorio.get(indice);
        return bucket.removerBiblioteca(clienteId);
    }
    
    private void split(int indice) {
        BucketBiblioteca bucketAntigo = diretorio.get(indice);
        
        if (bucketAntigo.getProfundidadeLocal() == profundidadeGlobal) {
            duplicarDiretorio();
        }
        
        BucketBiblioteca novoBucket = new BucketBiblioteca(
            bucketAntigo.getProfundidadeLocal() + 1, 
            capacidadeBucket
        );
        bucketAntigo.setProfundidadeLocal(bucketAntigo.getProfundidadeLocal() + 1);
        
        List<Biblioteca> bibliotecasTemp = new ArrayList<>(bucketAntigo.getBibliotecas());
        bucketAntigo.getBibliotecas().clear();
        
        for (Biblioteca biblioteca : bibliotecasTemp) {
            int novoIndice = hash(biblioteca.getClienteId());
            if (novoIndice == indice) {
                bucketAntigo.adicionarBiblioteca(biblioteca);
            } else {
                novoBucket.adicionarBiblioteca(biblioteca);
            }
        }
        
        int indiceParceiro = indice ^ (1 << (profundidadeGlobal - 1));
        diretorio.set(indiceParceiro, novoBucket);
    }
    
    private void duplicarDiretorio() {
        profundidadeGlobal++;
        List<BucketBiblioteca> novoDiretorio = new ArrayList<>();
        
        for (BucketBiblioteca bucket : diretorio) {
            novoDiretorio.add(bucket);
            novoDiretorio.add(bucket);
        }
        
        diretorio = novoDiretorio;
    }
}

// Classe BucketBiblioteca
class BucketBiblioteca {
    private int profundidadeLocal;
    private int capacidade;
    private List<Biblioteca> bibliotecas;
    
    public BucketBiblioteca(int profundidadeLocal, int capacidade) {
        this.profundidadeLocal = profundidadeLocal;
        this.capacidade = capacidade;
        this.bibliotecas = new ArrayList<>();
    }
    
    public boolean isFull() { 
        return bibliotecas.size() >= capacidade; 
    }
    
    public void adicionarBiblioteca(Biblioteca biblioteca) {
        if (!isFull()) {
            bibliotecas.add(biblioteca);
        }
    }
    
    public boolean removerBiblioteca(int clienteId) {
        return bibliotecas.removeIf(bib -> bib.getClienteId() == clienteId);
    }
    
    public Biblioteca buscarBibliotecaPorCliente(int clienteId) {
        return bibliotecas.stream()
                         .filter(bib -> bib.getClienteId() == clienteId)
                         .findFirst()
                         .orElse(null);
    }
    
    public int getProfundidadeLocal() { return profundidadeLocal; }
    public void setProfundidadeLocal(int profundidadeLocal) { 
        this.profundidadeLocal = profundidadeLocal; 
    }
    public List<Biblioteca> getBibliotecas() { return bibliotecas; }
}
