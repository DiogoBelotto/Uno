package players;

import java.util.LinkedList;

import baralho.Carta;

public class Player {
    private final LinkedList<Carta> deck;
    private String nome;
    private boolean pronto;

    public Player() {
        deck = new LinkedList<>();
        pronto = false;
    }

    public void addCarta(Carta carta) {
        deck.add(carta);
    }

    public boolean removeCarta(Carta carta) {
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
}
