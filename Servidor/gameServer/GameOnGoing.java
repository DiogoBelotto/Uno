package gameServer;

import baralho.Baralho;
import baralho.Carta;
import baralho.CartaEspecial;
import baralho.CartaNormal;
import players.Player;

public class GameOnGoing implements Runnable {
    private Baralho baralho;
    private Carta cartaNaMesa;
    private int posicaoAtual;

    public boolean isOrdemParaDireita() {
        return isOrdemParaDireita;
    }

    public void setOrdemParaDireita(boolean ordemParaDireita) {
        isOrdemParaDireita = ordemParaDireita;
    }

    private boolean isOrdemParaDireita;

    public GameOnGoing() {
        posicaoAtual = 0;
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
            Server.playersSemaphore.acquire();
        } catch (InterruptedException e) {
            System.out.println("espera");
        }
        for (int i = 0; i < GameListener.players.size(); i++) {
            daCartasIniciais(GameListener.players.get(i));
        }
        //Libera o semaphore
        Server.playersSemaphore.release();

        //Coloca uma carta na mesa
        boolean cartaNaMesaValida = false;
        while (!cartaNaMesaValida) {
            //A carta na mesa precisa ser uma carta normal, pesca uma nova caso seja especial
            cartaNaMesa = baralho.pescaCarta();
            if (cartaNaMesa instanceof CartaNormal)
                cartaNaMesaValida = true;
        }


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
                    ClientHandler.clientHandlers.get(j).toAClient("07\t" + nome + "\t" + id + "\t" + quantCartas + "\n", j);
                }
            }
        }
        ClientHandler.clientHandlers.getFirst().toAllClient("06\t\n");
        isOrdemParaDireita = true;
        //Loop do jogo (em desenvolvimento)
        while (true) {
            if (GameListener.numPlayers == 0)
                break;
            if (baralho.getBaralho().isEmpty())
                baralho.criaBaralho();

            //Se Algum jogador ganhar
            for (int i = 0; i < GameListener.players.size(); i++) {
                if (ClientHandler.clientHandlers.get(i).getPlayer().getDeck().isEmpty()) {
                    ClientHandler.clientHandlers.get(i).toAllClient("13\t" + ClientHandler.clientHandlers.get(i).getPlayer().getId() + "\n");
                }
            }

            //Se algum oponente gritou uno, remove isso e avisa aos jogadores antes
            if (GameListener.oponenteGritouUno) {
                GameListener.oponenteGritouUno = false;
                GameListener.enviaGritouUno(null, false);
                for (int i = 0; i < GameListener.players.size(); i++) {
                    if (ClientHandler.clientHandlers.get(i).getPlayer().isGritouUno()) {
                        String[] j = {String.valueOf(ClientHandler.clientHandlers.get(i).getId())};
                        GameListener.enviaGritouUno(j, true);
                    }
                }

                //Enviar ao jogador que o oponente não gritou mais uno
            }


            if (!GameListener.jogadaCartaEspecial) {
                //Comunica o player que é sua rodada
                ClientHandler.clientHandlers.get(posicaoAtual).toAClient("08\t1\n", posicaoAtual);
                //Envia aos demais players que não é sua rodada
                for (int i = 0; i < GameListener.players.size(); i++) {
                    if (i != posicaoAtual) {
                        ClientHandler.clientHandlers.get(i).toAClient("08\t0\n", i);
                    }
                }
            } else GameListener.jogadaCartaEspecial = false;


            posicaoAtual = posicaoJogadorAtual(false);


            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    System.out.println("espera");
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
        ClientHandler.clientHandlers.get(i).toAllClient("05\t" + valor + "\t" + cor + "\n");
    }


    public int posicaoJogadorAtual(boolean jogadorAnterior) {
        boolean isOrdemParaDireitaInterna;
        if (jogadorAnterior && isOrdemParaDireita) {
            isOrdemParaDireitaInterna = false;
        } else if (jogadorAnterior) {
            isOrdemParaDireitaInterna = true;
        } else isOrdemParaDireitaInterna = isOrdemParaDireita;

        if ((posicaoAtual == GameListener.numPlayers - 1) && isOrdemParaDireitaInterna)
            return 0;
        if ((posicaoAtual == 0) && !isOrdemParaDireitaInterna)
            return GameListener.numPlayers - 1;
        if (isOrdemParaDireitaInterna)
            return posicaoAtual + 1;
        return posicaoAtual - 1;

    }

    public Carta getCartaNaMesa() {
        return cartaNaMesa;
    }

    public void setCartaNaMesa(Carta cartaNaMesa) {
        this.cartaNaMesa = cartaNaMesa;
    }


    public void pescaCarta(int i) {
        String valor, cor;
        if (baralho.getBaralho().isEmpty())
            baralho.criaBaralho();
        Carta carta = baralho.getBaralho().poll();
        if (carta instanceof CartaEspecial) {
            valor = ((CartaEspecial) carta).getTipoEspecial();
        } else {
            assert carta != null;
            valor = "" + ((CartaNormal) carta).getNumero();
        }
        cor = carta.getCor();
        ClientHandler.clientHandlers.get(i).toAClient("04\t" + valor + "\t" + cor + "\n", i);
        ClientHandler.clientHandlers.get(i).getPlayer().addCarta(carta);
    }

    public int getPosicaoAtual() {
        return posicaoAtual;
    }

    public void setPosicaoAtual(int posicaoAtual) {
        this.posicaoAtual = posicaoAtual;
    }
}
