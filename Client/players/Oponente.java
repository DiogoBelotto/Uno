package players;

public class Oponente {
  private String nome;
  private int id, quantCartas;
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
  public void setNome(String nome) {
    this.nome = nome;
  }
  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
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

  public void setGritouUno(boolean gritouUno) {
    this.gritouUno = gritouUno;
  }
}
