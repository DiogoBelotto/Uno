package gameServer;

import java.util.concurrent.Semaphore;

import baralho.Baralho;
import baralho.Carta;
import baralho.CartaEspecial;
import baralho.CartaNormal;
import players.Player;

public class GameOnGoing implements Runnable {
    private Baralho baralho;
    private Carta cartaNaMesa;
    private final Semaphore semaphore;
    private int posicaoAtual;
    private boolean isOrdemParaDireita;

    public GameOnGoing() {
        posicaoAtual = 0;
        semaphore = new Semaphore(1);
        baralho = new Baralho();
    }

    //Da 7 cartas para o Jogador recebido por parametro
    public void daCartasIniciais(Player player) {
        for (int i = 0; i < 7; i++) {
            player.addCarta(baralho.pescaCarta());
        }
    }

    //Logica do Programa
    @Override
    public void run() {
        //Aciona um semaphore para acessar a lista de players e dar 7 cartas para cada via metodo
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            System.out.println("espera");
        }
        for (int i = 0; i < GameListener.players.size(); i++) {
            daCartasIniciais(GameListener.players.get(i));
        }
        semaphore.release();
        //Libera o semaphore

        //Coloca uma carta na mesa
        cartaNaMesa = baralho.pescaCarta();


        //Mostra o Deck inicial de cada jogador (Usado para debug)
        System.out.println(baralho.getBaralho().size());
        for (int i = 0; i < GameListener.players.size(); i++) {
            System.out.println(GameListener.players.get(i).getDeck());
        }

        //Envia as cartas recebidas para os Players via sockets
        String valor, cor;
        for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
            for (int j = 0; j < ClientHandler.clientHandlers.get(i).getPlayer().getDeck().size(); j++) {
                if (ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(j) instanceof CartaEspecial) {
                    valor = ((CartaEspecial) ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(j)).getTipoEspecial();
                } else {
                    valor = "" + ((CartaNormal) ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(j)).getNumero();
                }
                cor = ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(j).getCor();
                ClientHandler.clientHandlers.get(i).toAClient("04\t" + valor + "\t" + cor + "\n", i);
            }


            //Envia Carta na mesa
            enviaCartaNaMesa(i);
        }

        //Envia oponentes
        for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
            for (int j = 0; j < ClientHandler.clientHandlers.size(); j++) {
                if (!ClientHandler.clientHandlers.get(i).equals(ClientHandler.clientHandlers.get(j))) {
                    String nome = ClientHandler.clientHandlers.get(i).getPlayer().getNome();
                    int id = ClientHandler.clientHandlers.get(i).getPlayer().getId();
                    int quantCartas = ClientHandler.clientHandlers.get(i).getPlayer().getDeck().size();
                    ClientHandler.clientHandlers.get(i).toAClient("07\t" + nome + "\t" + id + "\t" + quantCartas + "\n", i);
                }
            }
        }
        ClientHandler.clientHandlers.getFirst().toAllClient("06\t\n");

        //Loop do jogo (em desenvolvimento)
        while (true) {
            if (GameListener.numPlayers == 0)
                break;
            if (baralho.getBaralho().isEmpty())
                baralho.criaBaralho();

            //Comunica o player que é sua rodada
            ClientHandler.clientHandlers.get(posicaoAtual).toAClient("08\t1\n", posicaoAtual);
            //Envia aos demais players que não é sua rodada
            for (int i = 0; i < GameListener.players.size(); i++) {
                if (i != posicaoAtual) {
                    ClientHandler.clientHandlers.get(i).toAClient("08\t0\n", i);
                }
            }

            posicaoAtual = posicaoJogadorAtual();


            while (!ClientHandler.temNovaMensagem) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("sleep exeption");
                }
            }

        }

        System.out.println("Jogo terminou!");
    }

    public void enviaCartaNaMesa(int i) {
        String valor;
        String cor;
        if (cartaNaMesa instanceof CartaEspecial) {
            valor = ((CartaEspecial) cartaNaMesa).getTipoEspecial();
        } else {
            valor = "" + ((CartaNormal) cartaNaMesa).getNumero();
        }
        cor = cartaNaMesa.getCor();
        ClientHandler.clientHandlers.get(i).toAClient("05\t" + valor + "\t" + cor + "\n", i);
    }


    public int posicaoJogadorAtual() {
        if ((posicaoAtual == GameListener.numPlayers - 1) && isOrdemParaDireita)
            return 0;
        if ((posicaoAtual == 0) && !isOrdemParaDireita)
            return GameListener.numPlayers - 1;
        if (isOrdemParaDireita)
            return posicaoAtual++;
        return posicaoAtual--;
    }

    public Carta getCartaNaMesa() {
        return cartaNaMesa;
    }

    public void setCartaNaMesa(Carta cartaNaMesa) {
        this.cartaNaMesa = cartaNaMesa;
    }

    public Baralho getBaralho() {
        return baralho;
    }

    public void setBaralho(Baralho baralho) {
        this.baralho = baralho;
    }

    public void pescaCarta(int i){
        String valor,cor;
        if (baralho.getBaralho().isEmpty())
            baralho.criaBaralho();
        Carta carta = baralho.getBaralho().poll();
        if (carta instanceof CartaEspecial) {
            valor = ((CartaEspecial) carta).getTipoEspecial();
        } else {
            valor = "" + ((CartaNormal) carta).getNumero();
        }
        cor = carta.getCor();
        ClientHandler.clientHandlers.get(i).toAClient("04\t" + valor + "\t" + cor + "\n", i);
        ClientHandler.clientHandlers.get(i).getPlayer().addCarta(carta);
    }
}
