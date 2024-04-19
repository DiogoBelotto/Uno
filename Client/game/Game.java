package game;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import baralho.Carta;
import baralho.CartaEspecial;
import baralho.CartaNormal;
import players.Oponente;

public class Game implements Runnable {
    private final Client client;
    private boolean podeComecar;
    private int numPlayers;
    private Carta cartaNaMesa;
    private boolean isSuaRodada;
    private final ArrayList<Oponente> opontentes;
    private boolean sairDoJogo;
    private boolean oponenteGritouUNo;
    private final Screen screen;
    public boolean oponenteGritouUno;

    public Game(Screen screen) throws IOException {
        oponenteGritouUNo = false;
        sairDoJogo = false;
        opontentes = new ArrayList<>();
        isSuaRodada = false;
        podeComecar = false;
        client = new Client(new Socket("localhost", 6789));
        client.escutaMensagem();
        this.screen = screen;
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
        boolean gritouUno;

        Queue<String> mensagensRecebidas = new LinkedList<>();


        do {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                Screen.mensagensSemaphore.acquire();
            } catch (InterruptedException e) {
                System.out.println("Semaphore Exception");
            }
            if (client.getMensagensRecebidas().peek() != null)
                if (client.getMensagensRecebidas().isEmpty())
                    client.getMensagensRecebidas().poll();

            if (!(client.getMensagensRecebidas().isEmpty()) && client.getMensagensRecebidas().peek() != null) {
                for (int i = 0; i < client.getMensagensRecebidas().size(); i++) {
                    try {
                        mensagensRecebidas.add(client.getMensagensRecebidas().remove());
                    } catch (Exception e) {
                        System.out.println("Erro ao receber mensagensRecebidas");
                    }
                }
            }
            Screen.mensagensSemaphore.release();

            if (mensagensRecebidas.peek() != null)
                if (mensagensRecebidas.peek().equals(""))
                    mensagensRecebidas.poll();
            if (!mensagensRecebidas.isEmpty() && mensagensRecebidas.peek() != null) {
                mensagemRecebida = mensagensRecebidas.poll().split("\t");
                switch (Integer.parseInt(mensagemRecebida[0])) {
                    // Alteração no Numero de Players
                    case 1:
                        numPlayers = Integer.parseInt(mensagemRecebida[1]);
                        break;
                    // Caso a partida ja esteja Lotada
                    case 2:
                        try {
                            Screen.tipoDePrintSemaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        Screen.tipoDePrint = 2;
                        Screen.tipoDePrintSemaphore.release();
                        break;
                    // Entrada aceita
                    case 3:
                        try {
                            Screen.tipoDePrintSemaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        Screen.tipoDePrint = 1;
                        Screen.tipoDePrintSemaphore.release();
                        break;
                    // Pesca Carta
                    case 4:
                        // Verifica o valor/tipo e cor da carta recebida
                        valor = mensagemRecebida[1];
                        cor = mensagemRecebida[2];

                        // Caso seja uma Carta Especial
                        carta = verificaTipoDeCarta(valor, cor);
                        try {
                            Screen.geralSemaphore.acquire();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        client.getPlayer().addCarta(carta);
                        Screen.geralSemaphore.release();
                        break;

                    // Carta na Mesa
                    case 5:

                        // Verifica o valor/tipo e cor da carta recebida
                        valor = mensagemRecebida[1];
                        cor = mensagemRecebida[2];

                        // Caso seja uma Carta Especial
                        carta = verificaTipoDeCarta(valor, cor);

                        cartaNaMesa = carta;

                        break;
                    // Jogo iniciado
                    case 6:
                        try {
                            Screen.podeComecarSemaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }

                        podeComecar = true;
                        Screen.podeComecarSemaphore.release();
                        break;
                    // Adiciona Oponentes
                    case 7:
                        try {
                            Screen.geralSemaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        String nome = mensagemRecebida[1];
                        id = Integer.parseInt(mensagemRecebida[2]);
                        quantCartas = Integer.parseInt(mensagemRecebida[3]);

                        opontentes.add(new Oponente(nome, id, quantCartas));

                        synchronized (screen) {
                            screen.notifyAll();
                        }

                        Screen.geralSemaphore.release();
                        break;

                    //Altera isSuaRodada
                    case 8:
                        int msg = Integer.parseInt(mensagemRecebida[1]);

                        isSuaRodada = msg == 1;

                        synchronized (screen) {
                            screen.notifyAll();
                        }


                        break;
                    //Pergunta Cor
                    case 10:
                        try {

                            Screen.geralSemaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        id = Integer.parseInt(mensagemRecebida[1]);
                        quantCartas = Integer.parseInt(mensagemRecebida[2]);

                        for (Oponente opontente : opontentes) {
                            if (opontente.getId() == id) {
                                opontente.setQuantCartas(quantCartas);
                            }
                        }

                        Screen.geralSemaphore.release();
                        break;
                    //Jogador gritou uno
                    case 11:
                        id = Integer.parseInt(mensagemRecebida[1]);
                        gritouUno = Boolean.parseBoolean(mensagemRecebida[2]);

                        for (Oponente opontente : opontentes) {
                            if (opontente.getId() == id) {
                                opontente.setGritouUnoParasi(gritouUno);
                            }
                        }

                        break;
                    //Oponente gritou uno
                    case 12:
                        gritouUno = Boolean.parseBoolean(mensagemRecebida[1]);

                        oponenteGritouUNo = gritouUno;
                        break;

                    case 13:
                        sairDoJogo = true;
                        try {
                            Screen.tipoDePrintSemaphore.acquire();
                        } catch (InterruptedException e) {
                            System.out.println("Semaphore Exception");
                        }
                        Screen.tipoDePrint = 3;
                        Screen.tipoDePrintSemaphore.release();

                        synchronized (screen) {
                            screen.notifyAll();
                        }

                        for (Oponente opontente : opontentes) {
                            if ((opontente.getId() == Integer.parseInt(mensagemRecebida[1]))) {
                                System.out.println("\u001B[32m" + "\nO Player " + opontente.getNome() + " ganhou! \u001B[0m");
                                return;
                            }
                        }
                        System.out.println("\u001B[32m" + "\nO Player " + client.getPlayer().getNome() + " ganhou! \u001B[0m");

                    case 14:
                        oponenteGritouUno = Boolean.parseBoolean(mensagemRecebida[1]);
                        break;
                }
            }
        } while (!sairDoJogo);
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


    public boolean isOponenteGritouUNo() {
        return oponenteGritouUNo;
    }

    public void setSuaRodada(boolean suaRodada) {
        isSuaRodada = suaRodada;
    }
}
