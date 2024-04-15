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

    public void setTipoEspecial(TipoEspecial tipoEspecial) {
        this.tipoEspecial = tipoEspecial;
    }

    @Override
    public String toString() {
        switch (tipoEspecial) {
            case MAIS_2:
                return super.getStringCor() + "+2" + "\u001B[0m";
            case BLOQUEIO:
                return super.getStringCor() + "PJ" + "\u001B[0m";
            case INVERTE_ORDEM:
                return super.getStringCor() + "IN" + "\u001B[0m";
            case MAIS_4:
                return super.getStringCor() + "+4" + "\u001B[0m";
            case MUDA_COR:
                return super.getStringCor() + "MC" + "\u001B[0m";
        }
        return "";
    }

    public static Cor getCor(String cor) {
        if (cor.equals("azul"))
            return Cor.AZUL;
        if (cor.equals("verde"))
            return Cor.VERDE;
        if (cor.equals("AMARELO"))
            return Cor.AMARELO;
        return Cor.VERMELHO;
    }
}
