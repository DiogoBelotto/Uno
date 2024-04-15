package players;

import java.util.LinkedList;

import baralho.Carta;

public class Player {
    private LinkedList<Carta> deck;
    private String nome;
    private boolean pronto;
    private int id;

    public Player(String nome) {
        pronto = false;
        this.nome = nome;
        deck = new LinkedList<Carta>();
    }

    public void addCarta(Carta carta){
        deck.add(carta);
    }

    public boolean removeCarta(Carta carta){
        return deck.remove(carta);
    }

    @Override
    public String toString() {
        return deck.toString();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LinkedList<Carta> getDeck() {
        return deck;
    }

    public boolean isPronto() {
        return pronto;
    }

    public void setPronto(boolean pronto) {
        this.pronto = pronto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    
}
