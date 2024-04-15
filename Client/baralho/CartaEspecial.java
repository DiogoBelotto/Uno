package baralho;

public class CartaEspecial extends Carta {
  private TipoEspecial tipoEspecial;

  public CartaEspecial(Cor cor, TipoEspecial tipoEspecial) {
    super(cor);
    this.tipoEspecial = tipoEspecial;
  }

  public TipoEspecial getTipoEspecial() {
    return tipoEspecial;
  }

  public void setTipoEspecial(TipoEspecial tipoEspecial) {
    this.tipoEspecial = tipoEspecial;
  }

  public static TipoEspecial retornaTipo(String entrada) {
    if (entrada.equals("+2"))
      return TipoEspecial.MAIS_2;
    if (entrada.equals("IN"))
      return TipoEspecial.INVERTE_ORDEM;
    if (entrada.equals("PJ"))
      return TipoEspecial.BLOQUEIO;
    if (entrada.equals("+4"))
      return TipoEspecial.MAIS_4;
    return TipoEspecial.MUDA_COR;
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

}
