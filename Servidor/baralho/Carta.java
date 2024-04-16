package baralho;

public class Carta {
    private Cor cor;
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_YELLOW = "\u001B[93m";
    public static final String ANSI_NORMAL = "\u001B[40m";

    public Carta(Cor cor) {
        this.cor = cor;
    }

    public String getCor() {
        if (cor.equals(Cor.AMARELO))
            return "amarelo";
        if (cor.equals(Cor.AZUL))
            return "azul";
        if (cor.equals(Cor.VERDE))
            return "verde";
        if (cor.equals(Cor.VERMELHO))
            return "vermelho";
        return "sem_cor";
    }

    public void setCor(Cor cor) {
        this.cor = cor;
    }

    public String getStringCor() {
        return switch (this.cor) {
            case AZUL -> ANSI_BLUE;
            case AMARELO -> ANSI_YELLOW;
            case VERMELHO -> ANSI_RED;
            case VERDE -> ANSI_GREEN;
            case SEM_COR -> ANSI_NORMAL;
        };
    }

}