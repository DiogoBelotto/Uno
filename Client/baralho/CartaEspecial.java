package baralho;

public class CartaEspecial extends Carta {
    private final TipoEspecial tipoEspecial;

    public CartaEspecial(Cor cor, TipoEspecial tipoEspecial) {
        super(cor);
        this.tipoEspecial = tipoEspecial;
    }

    public TipoEspecial getTipoEspecial() {
        return tipoEspecial;
    }


    public static TipoEspecial retornaTipo(String entrada) {
        return switch (entrada) {
            case "+2" -> TipoEspecial.MAIS_2;
            case "IN" -> TipoEspecial.INVERTE_ORDEM;
            case "PJ" -> TipoEspecial.BLOQUEIO;
            case "+4" -> TipoEspecial.MAIS_4;
            default -> TipoEspecial.MUDA_COR;
        };
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

}
