package players;

public class Oponente {
    private String nome;
    private final int id;
    private int quantCartas;
    private boolean gritouUno;

    public Oponente(String nome, int id, int quantCartas) {
        gritouUno = false;
        this.nome = nome;
        this.id = id;
        this.quantCartas = quantCartas;
    }

    public String getNome() {
        return nome;
    }

    public int getId() {
        return id;
    }

    public int getQuantCartas() {
        return quantCartas;
    }

    public void setQuantCartas(int quantCartas) {
        this.quantCartas = quantCartas;
    }


    public boolean isGritouUno() {
        return gritouUno;
    }

}
