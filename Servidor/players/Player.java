package players;

import java.util.LinkedList;

import baralho.Carta;

public class Player {
    private final LinkedList<Carta> deck;
    private final String nome;
    private boolean pronto;
    private int id;
    private boolean gritouUno;

    public Player(String nome) {
        gritouUno = false;
        pronto = false;
        this.nome = nome;
        deck = new LinkedList<Carta>();
    }

    public void addCarta(Carta carta){
        deck.add(carta);
    }

    @Override
    public String toString() {
        return deck.toString();
    }

    public String getNome() {
        return nome;
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


    public boolean isGritouUno() {
        return gritouUno;
    }

    public void setGritouUno(boolean gritouUno) {
        this.gritouUno = gritouUno;
    }
}
