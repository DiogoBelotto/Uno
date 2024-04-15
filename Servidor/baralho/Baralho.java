package baralho;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class Baralho {
  private Queue<Carta> baralho;
  private LinkedList<Carta> baralhoTemp;

  public Baralho() {
    baralho = new LinkedList<Carta>();
    baralhoTemp = new LinkedList<Carta>();
    
    criaBaralho();
  }

  public void criaBaralho(){
    criaCartasNormal(Cor.VERMELHO);
    criaCartasNormal(Cor.AZUL);
    criaCartasNormal(Cor.VERDE);
    criaCartasNormal(Cor.AMARELO);
    
    criaCartasEspecial(TipoEspecial.BLOQUEIO);
    criaCartasEspecial(TipoEspecial.MAIS_2);
    criaCartasEspecial(TipoEspecial.INVERTE_ORDEM);

    criaCartasEspecialIncolor(TipoEspecial.MAIS_4);
    criaCartasEspecialIncolor(TipoEspecial.MUDA_COR);

    embaralha();
    for(int i =0; i<baralhoTemp.size(); i++){
      baralho.add(baralhoTemp.get(i));
    }
    baralhoTemp.clear();
  }

  public void criaCartasNormal(Cor cor){
    for(int i=0; i<2; i++){
      int j;
      if(i ==0)
        j = 0;
      else
        j = 1;
      for(; j<10; j++){
        baralhoTemp.add(new CartaNormal(cor,j));
      }
    }
  }

  public void criaCartasEspecialAuxiliar(Cor cor, TipoEspecial tipoEspecial){
    for(int i=0; i<2; i++){
      baralhoTemp.add(new CartaEspecial(cor,tipoEspecial));
    }
  }
  
  public void criaCartasEspecial(TipoEspecial tipoEspecial){
    criaCartasEspecialAuxiliar(Cor.AMARELO, tipoEspecial);
    criaCartasEspecialAuxiliar(Cor.VERMELHO, tipoEspecial);
    criaCartasEspecialAuxiliar(Cor.VERDE, tipoEspecial);
    criaCartasEspecialAuxiliar(Cor.AZUL, tipoEspecial);
  }

  public void criaCartasEspecialIncolor(TipoEspecial tipoEspecial){
    for(int i=0; i<4; i++){
      baralhoTemp.add(new CartaEspecial(Cor.SEM_COR, tipoEspecial));
    }
  }

  public Carta pescaCarta(){
    return baralho.remove();
  }

  public void embaralha(){
    Collections.shuffle(baralhoTemp);
    return;
  }

  public void verificaSeHaCartas(){
    if(baralho.isEmpty()){
      criaBaralho();
    }
  }

  public Queue<Carta> getBaralho() {
    return baralho;
  }

  public void setBaralho(Queue<Carta> baralho) {
    this.baralho = baralho;
  }
}
