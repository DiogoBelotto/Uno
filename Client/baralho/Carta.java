package baralho;

public class Carta {
    private final Cor cor;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_YELLOW = "\u001B[93m";
    public static final String ANSI_SEM_COR = "\u001B[40m";

    public Carta(Cor cor) {
        this.cor = cor;
    }

    public Cor getCor() {
        return cor;
    }

    public String getStringCor() {
        return switch (this.cor) {
            case AZUL -> ANSI_BLUE;
            case AMARELO -> ANSI_YELLOW;
            case VERMELHO -> ANSI_RED;
            case VERDE -> ANSI_GREEN;
            case SEM_COR -> ANSI_SEM_COR;
        };
    }

    public static Cor retornaCor(String entrada) {
        return switch (entrada) {
            case "verde" -> Cor.VERDE;
            case "vermelho" -> Cor.VERMELHO;
            case "azul" -> Cor.AZUL;
            case "amarelo" -> Cor.AMARELO;
            default -> Cor.SEM_COR;
        };
    }

}