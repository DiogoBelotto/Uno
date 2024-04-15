package baralho;

public class CartaNormal extends Carta {
    private int numero;

    public CartaNormal(Cor cor, int numero) {
        super(cor);
        this.numero = numero;

    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return super.getStringCor() + numero + "\u001B[0m";
    }

}
