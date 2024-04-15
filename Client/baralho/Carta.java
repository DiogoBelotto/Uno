package baralho;

public class Carta {
  private Cor cor;
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

  public void setCor(Cor cor) {
    this.cor = cor;
  }

  public String getStringCor() {
    switch (this.cor) {
      case AZUL:
        return ANSI_BLUE;
      case AMARELO:
        return ANSI_YELLOW;
      case VERMELHO:
        return ANSI_RED;
      case VERDE:
        return ANSI_GREEN;
      case SEM_COR:
        return ANSI_SEM_COR;
    }
    return ANSI_SEM_COR;
  }

  public static Cor retornaCor(String entrada) {
    if (entrada.equals("verde"))
      return Cor.VERDE;
    if (entrada.equals("vermelho"))
      return Cor.VERMELHO;
    if (entrada.equals("azul"))
      return Cor.AZUL;
    if (entrada.equals("amarelo"))
      return Cor.AMARELO;
    return Cor.SEM_COR;
  }

}