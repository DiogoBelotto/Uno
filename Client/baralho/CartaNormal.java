package baralho;

public class CartaNormal extends Carta {
    private final int numero;

    public CartaNormal(Cor cor, int numero) {
        super(cor);
        this.numero = numero;

    }

    public int getNumero() {
        return numero;
    }

    @Override
    public String toString() {
        return super.getStringCor() + numero + "\u001B[0m";
    }

}
