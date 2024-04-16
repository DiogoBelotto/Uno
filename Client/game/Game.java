package game;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import baralho.Carta;
import baralho.CartaEspecial;
import baralho.CartaNormal;
import players.Oponente;

public class Game implements Runnable {
    private final Client client;
    private boolean podeComecar;
    private int numPlayers;
    private final Semaphore semaphore;
    private Carta cartaNaMesa;
    private boolean isSuaRodada;
    private ArrayList<Oponente> opontentes;
    private boolean novaRodada;
    private int rodadaAtual;

    public Game() throws IOException {
        rodadaAtual = 0;
        novaRodada = false;
        opontentes = new ArrayList<>();
        isSuaRodada = false;
        semaphore = new Semaphore(1);
        podeComecar = false;
        client = new Client(new Socket("localhost", 6789));
        client.escutaMensagem();
    }

    public Client getClient() {
        return client;
    }

    @Override
    public void run() {
        // Inicializando variaveis utilizadas nos cases
        String[] mensagemRecebida;
        String valor, cor;
        Carta carta;
        int id, quantCartas;

        Queue<String> mensagensRecebidas = new LinkedList<>();

        while (true) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                System.out.println("Semaphore Exception");
            }
            if (client.getMensagensRecebidas().peek() != null)
                if (client.getMensagensRecebidas().peek().equals(""))
                    client.getMensagensRecebidas().poll();

            if (!(client.getMensagensRecebidas().size() == 0) && client.getMensagensRecebidas().peek() != null) {
                for (int i = 0; i < client.getMensagensRecebidas().size(); i++) {
                    try {
                        mensagensRecebidas.add(client.getMensagensRecebidas().remove());
                        System.out.println("lendo msgs de entrada!");
                    } catch (Exception e) {
                        System.out.println("Erro ao receber mensagensRecebidas");
                    }
                }
            }
            semaphore.release();

            if (mensagensRecebidas.peek() != null)
                if (mensagensRecebidas.peek().equals(""))
                    mensagensRecebidas.poll();
            if (!mensagensRecebidas.isEmpty() && mensagensRecebidas.peek() != null) {
                System.out.println("\u001B[32m" + "msg: " + mensagensRecebidas.peek() + "\u001B[0m");
                mensagemRecebida = mensagensRecebidas.poll().split("\t");
                switch (Integer.parseInt(mensagemRecebida[0])) {
                    // Alteração no Numero de Players
                    case 1:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        numPlayers = Integer.parseInt(mensagemRecebida[1]);
                        semaphore.release();
                        break;
                    // Caso a partida ja esteja Lotada
                    case 2:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        Screen.tipoDePrint = 2;
                        semaphore.release();
                        break;
                    // Entrada aceita
                    case 3:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        Screen.tipoDePrint = 1;
                        semaphore.release();
                        break;
                    // Pesca Carta
                    case 4:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }

                        // Verifica o valor/tipo e cor da carta recebida
                        valor = mensagemRecebida[1];
                        cor = mensagemRecebida[2];

                        // Caso seja uma Carta Especial
                        carta = verificaTipoDeCarta(valor, cor);

                        client.getPlayer().addCarta(carta);

                        semaphore.release();
                        break;

                    // Carta na Mesa
                    case 5:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        // Verifica o valor/tipo e cor da carta recebida
                        valor = mensagemRecebida[1];
                        cor = mensagemRecebida[2];

                        // Caso seja uma Carta Especial
                        carta = verificaTipoDeCarta(valor, cor);

                        cartaNaMesa = carta;

                        semaphore.release();
                        break;
                    // Jogo iniciado
                    case 6:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }

                        podeComecar = true;
                        semaphore.release();
                        break;
                    // Adiciona Oponentes
                    case 7:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        String nome = mensagemRecebida[1];
                        id = Integer.parseInt(mensagemRecebida[2]);
                        quantCartas = Integer.parseInt(mensagemRecebida[3]);

                        System.out.println("Novo oponente: " + nome + ", ID: " + id + ", Quantidade de Cartas: " + quantCartas);
                        opontentes.add(new Oponente(nome, id, quantCartas));

                        semaphore.release();
                        break;

                    //Altera isSuaRodada
                    case 8:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }

                        int msg = Integer.parseInt(mensagemRecebida[1]);

                        if (msg == 1)
                            isSuaRodada = true;
                        else
                            isSuaRodada = false;

                        rodadaAtual++;
                        novaRodada = true;

                        semaphore.release();
                        break;
                    //Pergunta Cor
                    case 9:
                        break;
                        //implementar
                    //Atualização na quantidade de cartas
                    case 10:
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        id = Integer.parseInt(mensagemRecebida[1]);
                        quantCartas = Integer.parseInt(mensagemRecebida[2]);

                        for (Oponente opontente : opontentes) {
                            if (opontente.getId() == id) {
                                opontente.setQuantCartas(quantCartas);
                                System.out.println(opontente.getQuantCartas());
                            }
                        }


                        semaphore.release();
                        break;

                }
            }
        }
    }

    private Carta verificaTipoDeCarta(String valor, String cor) {
        Carta carta;
        if (valor.equals("MC") || valor.equals("+2") || valor.equals("+4") || valor.equals("IN")
                || valor.equals("PJ")) {
            carta = new CartaEspecial(Carta.retornaCor(cor), CartaEspecial.retornaTipo(valor));
        } // Caso seja carta Normal
        else {
            carta = new CartaNormal(Carta.retornaCor(cor), Integer.parseInt(valor));
        }
        return carta;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public Carta getCartaNaMesa() {
        return cartaNaMesa;
    }

    public boolean isSuaRodada() {
        return isSuaRodada;
    }

    public ArrayList<Oponente> getOpontentes() {
        return opontentes;
    }

    public boolean isPodeComecar() {
        return podeComecar;
    }

    public void setPodeComecar(boolean podeComecar) {
        this.podeComecar = podeComecar;
    }

    public int getRodadaAtual() {
        return rodadaAtual;
    }

    public void setRodadaAtual(int rodadaAtual) {
        this.rodadaAtual = rodadaAtual;
    }

    public boolean isNovaRodada() {
        return novaRodada;
    }

    public void setNovaRodada(boolean novaRodada) {
        this.novaRodada = novaRodada;
    }
}
