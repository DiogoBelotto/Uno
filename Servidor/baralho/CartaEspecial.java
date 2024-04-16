package baralho;

public class CartaEspecial extends Carta {
    private TipoEspecial tipoEspecial;

    public CartaEspecial(Cor cor, TipoEspecial tipoEspecial) {
        super(cor);
        this.tipoEspecial = tipoEspecial;
    }

    public String getTipoEspecial() {
        if (tipoEspecial.equals(TipoEspecial.BLOQUEIO))
            return "PJ";
        if (tipoEspecial.equals(TipoEspecial.INVERTE_ORDEM))
            return "IN";
        if (tipoEspecial.equals(TipoEspecial.MUDA_COR))
            return "MC";
        if (tipoEspecial.equals(TipoEspecial.MAIS_2))
            return "+2";
        return "+4";
    }

    @Override
    public String toString() {
        return switch (tipoEspecial) {
            case MAIS_2 -> super.getStringCor() + "+2" + "\u001B[0m";
            case BLOQUEIO -> super.getStringCor() + "PJ" + "\u001B[0m";
            case INVERTE_ORDEM -> super.getStringCor() + "IN" + "\u001B[0m";
            case MAIS_4 -> super.getStringCor() + "+4" + "\u001B[0m";
            case MUDA_COR -> super.getStringCor() + "MC" + "\u001B[0m";
        };
    }

    public static Cor getCor(String cor) {
        return switch (cor) {
            case "azul" -> Cor.AZUL;
            case "verde" -> Cor.VERDE;
            case "AMARELO" -> Cor.AMARELO;
            default -> Cor.VERMELHO;
        };
    }
}
