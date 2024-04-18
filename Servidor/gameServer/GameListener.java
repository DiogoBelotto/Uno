package gameServer;

import java.util.LinkedList;
import java.util.Queue;

import baralho.CartaEspecial;
import players.Player;

//Classe responsável por Tratar as mensagem recebida pelos sockets do ClientHandler
public class GameListener implements Runnable {

    public static int numPlayers;
    private boolean jogoIniciado;
    private int totalProntos;
    private final GameOnGoing gameOnGoing;
    public static LinkedList<Player> players;
    public static boolean gameTemNovJogada;
    public static boolean jogadaCartaEspecial;
    public static boolean oponenteGritouUno;

    public GameListener() {
        oponenteGritouUno = false;
        jogadaCartaEspecial = false;
        gameTemNovJogada = false;
        gameOnGoing = new GameOnGoing();
        players = new LinkedList<>();
        numPlayers = 0;
        jogoIniciado = false;
    }

    @Override
    public void run() {
        // Semaphore para critic sessions

        // Loop que verifica a existencia de mensagem escitas pelo ClienteHandler na
        // variavel estatica novaMensagem e temNovaMensage
        Queue<String> newMessages = new LinkedList<>();
        while (true) {
            // Caso o jogo ainda não foi iniciado e existam no minimo dois players com
            // status pronto inicia a uma nova thread na Clase GameOnGoing
            // responsável por controlar a lógica do jogo
            if (!jogoIniciado)
                if (numPlayers > 1 && totalProntos == numPlayers) {
                    jogoIniciado = true;
                    Thread th1 = new Thread(gameOnGoing);
                    th1.start();
                }
            // Requesita o semaphore antes de entrar no switch e finaliza ao seu final
            // visto que acessam e modificam muitas vezes as variaveis em seção critica
            try {
                Server.mensagensSemaphore.acquire();
            } catch (InterruptedException e) {
                System.out.println("Sempahore Exception!");
            }


            // Verifica se há alguma mensagem nova escutada pelos sockets
            if (ClientHandler.temNovaMensagem && ClientHandler.novasMensagens.peek() != null
                    && !ClientHandler.novasMensagens.peek().equals("")) {
                // Divide a mensagem em um array de Strings, a posição 0 é o id do
                // ClientHandler/Player de Origem, a posição 1 é o identificador do tipo de
                // ação a posição 2 a mensagem em si
                for (int i = 0; i < ClientHandler.novasMensagens.size(); i++) {
                    newMessages.add(ClientHandler.novasMensagens.poll());
                }
                System.out.println("\u001B[32m" + "Mensagem de entrada: " + newMessages.peek() + "\u001B[0m");
                String[] clientMessage = newMessages.poll().split("\t");
                Player player;
                if (newMessages.isEmpty())
                    ClientHandler.temNovaMensagem = false;

                if (clientMessage.length > 1) {

                    switch (Integer.parseInt(clientMessage[1])) {
                        // Identificador 1: Criar Jogador com nome recebido por mensagem
                        case 1:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    ClientHandler.clientHandlers.get(i)
                                            .setPlayer(new Player(clientMessage[2]));
                                    player = ClientHandler.clientHandlers.get(i).getPlayer();
                                    player.setId(ClientHandler.clientHandlers.get(i).getId());

                                    try {
                                        Server.playersSemaphore.acquire();
                                        Server.numPlayersSemaphore.acquire();
                                    } catch (InterruptedException e) {
                                        System.out.println("Sempahore Exception!");
                                    }

                                    players.add(player);

                                    Server.playersSemaphore.release();

                                    System.out.println(player.getNome());
                                    System.out.println("player ENTROU! total de palyers: " + numPlayers);
                                    ClientHandler.clientHandlers.getFirst().toAllClient("01\t" + numPlayers + "\n");

                                    Server.numPlayersSemaphore.release();
                                }

                            }
                            break;

                        // Identificador 2: Saída de Jogador
                        case 2:
                            try {
                                Server.numPlayersSemaphore.acquire();
                                Server.playersSemaphore.acquire();
                            } catch (InterruptedException e) {
                                System.out.println("Sempahore Exception!");
                            }

                            for (int i = 0; i < players.size(); i++) {
                                if (players.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    if (players.get(i).isPronto())
                                        totalProntos--;
                                    players.remove(i);
                                }
                            }
                            Server.playersSemaphore.release();
                            numPlayers--;
                            if (ClientHandler.clientHandlers != null && !ClientHandler.clientHandlers.isEmpty())
                                ClientHandler.clientHandlers.getFirst().toAllClient("01\t" + numPlayers + "\n");
                            System.out.println("player SAIU! total de players: " + numPlayers);

                            Server.numPlayersSemaphore.release();
                            break;

                        // Identificador 3: Jogador deu pronto
                        case 3:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    boolean isPronto = clientMessage[2].equals("true");
                                    if (isPronto) {
                                        try {
                                            Server.numPlayersSemaphore.acquire();
                                        } catch (InterruptedException e) {
                                            System.out.println("Sempahore Exception!");
                                        }
                                        ClientHandler.clientHandlers.get(i).getPlayer().setPronto(true);
                                        totalProntos++;
                                        System.out.println(
                                                "Player " + ClientHandler.clientHandlers.get(i).getPlayer().getNome()
                                                        + " está pronto! \nFaltam " + (numPlayers - totalProntos)
                                                        + " players darem pronto para o jogo iniciar!");
                                        System.out.println(
                                                (numPlayers == 1) ? "Mas nao é possivel jogar sozinho!" : "\r");
                                        Server.numPlayersSemaphore.release();
                                    }

                                }

                            }
                            break;
                        //Pescar carta
                        case 4:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    if (ClientHandler.clientHandlers.get(i).getPlayer().getDeck().size() == 1)
                                        ClientHandler.clientHandlers.get(i).getPlayer().setGritouUno(false);
                                    gameOnGoing.pescaCarta(i);
                                }

                            }

                            //Envia para os oponentes a quantidade de cartas atualizada
                            enviaQuantCartas(clientMessage, false);
                            synchronized (gameOnGoing) {
                                gameOnGoing.notify();
                            }
                            break;
                        //Jogar carta  normal
                        case 5:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                //Informa, primeiramente, para todos os  jogadores que não é mais sua vez, para garantir que ele não entre em um scanner nextLine
                                ClientHandler.clientHandlers.get(i).toAClient("08\t0\n", i);
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    gameOnGoing.setCartaNaMesa(ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                                    gameOnGoing.enviaCartaNaMesa(i);
                                    ClientHandler.clientHandlers.get(i).getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));
                                }
                            }
                            enviaQuantCartas(clientMessage, false);

                            synchronized (gameOnGoing) {
                                gameOnGoing.notify();
                            }

                            break;
                        //Jogar carta  especial
                        case 6:
                            Player playerDaRodada = null;
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                //Informa, primeiramente, para todos os  jogadores que não é mais sua vez, para garantir que ele não entre em um scanner nextLine
                                ClientHandler.clientHandlers.get(i).toAClient("08\t0\n", i);
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    //Realiza a ação da carta especial
                                    playerDaRodada = ClientHandler.clientHandlers.get(i).getPlayer();
                                    if (("IN").equals(((CartaEspecial) playerDaRodada.getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial())) {
                                        gameOnGoing.setOrdemParaDireita(!gameOnGoing.isOrdemParaDireita());
                                        playerDaRodada = null;
                                    }
                                    if (playerDaRodada != null) {
                                        if (((CartaEspecial) playerDaRodada.getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial().equals("+2")) {
                                            gameOnGoing.pescaCarta(gameOnGoing.getPosicaoAtual());
                                            gameOnGoing.pescaCarta(gameOnGoing.getPosicaoAtual());
                                            gameOnGoing.setPosicaoAtual(gameOnGoing.posicaoJogadorAtual(false));
                                            System.out.println(gameOnGoing.getPosicaoAtual());
                                        } else if (((CartaEspecial) playerDaRodada.getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial().equals("+4")) {
                                            gameOnGoing.pescaCarta(gameOnGoing.getPosicaoAtual());
                                            gameOnGoing.pescaCarta(gameOnGoing.getPosicaoAtual());
                                            gameOnGoing.pescaCarta(gameOnGoing.getPosicaoAtual());
                                            gameOnGoing.pescaCarta(gameOnGoing.getPosicaoAtual());
                                            gameOnGoing.setPosicaoAtual(gameOnGoing.posicaoJogadorAtual(false));
                                            System.out.println(gameOnGoing.getPosicaoAtual());
                                        } else if (((CartaEspecial) playerDaRodada.getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial().equals("PJ")) {
                                            gameOnGoing.setPosicaoAtual(gameOnGoing.posicaoJogadorAtual(false));
                                        }
                                    }
                                }
                            }
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                //Seta carta na Mesa, e caso for uma carta de escolher cor altera sua cor também
                                if (clientMessage.length == 4) {
                                    if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                        ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])).setCor(CartaEspecial.getCor(clientMessage[3]));
                                    }
                                }
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    gameOnGoing.setCartaNaMesa(ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                                    gameOnGoing.enviaCartaNaMesa(i);
                                    boolean isMudaCor = false;
                                    if (playerDaRodada != null) {
                                        isMudaCor = ((CartaEspecial) playerDaRodada.getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial().equals("MC");
                                    }

                                    ClientHandler.clientHandlers.get(i).getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));
                                    //Atualiza quantidade de cartas dos oponenetes e para si mesmo
                                    if (!isMudaCor) {
                                        enviaQuantCartas(clientMessage, true);
                                    }
                                }
                            }
                            enviaQuantCartas(clientMessage, false);
                            String[] idJogadorQuePescou = {String.valueOf(gameOnGoing.posicaoJogadorAtual(true))};
                            enviaQuantCartas(idJogadorQuePescou, false);

                            //Envia aos players que a informação de que há uma nova rodada, apenas se for uma carta especial do tipo +2, +4 ou PJ, que vão pular um jogador
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    if (playerDaRodada != null) {
                                        jogadaCartaEspecial = true;
                                        //Comunica o player que é sua rodada
                                        ClientHandler.clientHandlers.get(gameOnGoing.getPosicaoAtual()).toAClient("08\t1\n", gameOnGoing.getPosicaoAtual());
                                        //Envia aos demais players que não é sua rodada
                                        for (int j = 0; j < GameListener.players.size(); j++) {
                                            if (j != gameOnGoing.getPosicaoAtual()) {
                                                ClientHandler.clientHandlers.get(j).toAClient("08\t0\n", j);
                                            }
                                        }
                                    }
                                }
                            }


                            synchronized (gameOnGoing) {
                                gameOnGoing.notify();
                            }

                            break;
                        //Grita uno para si mesmo
                        case 7:
                            //Caso nenhum oponente tenha gritado uno antes
                            if (!oponenteGritouUno) {
                                for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                    if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                        ClientHandler.clientHandlers.get(i).getPlayer().setGritouUno(true);
                                        //Enviar aos oponentes o estado de gritouUno do player
                                        enviaGritouUno(clientMessage, true);
                                    }
                                }
                            }


                            break;
                        //Grita uno para oponente
                        case 8:
                            //Caso o jogador não tenha gritado uno antes
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getPlayer().getDeck().size() == 1) {
                                    if (!ClientHandler.clientHandlers.get(i).getPlayer().isGritouUno()) {
                                        oponenteGritouUno = true;
                                        gameOnGoing.pescaCarta(i);
                                        gameOnGoing.pescaCarta(i);
                                        //Enviar para o jogador que o oponente gritou uno
                                        enviaGritouUno(clientMessage, false);
                                    }
                                }
                            }

                            break;
                    }
                }

            }
            // Libera o semaphore após acessar as condições de corrida
            Server.mensagensSemaphore.release();
        }
    }

    private void enviaQuantCartas(String[] clientMessage, boolean siProprio) {
        if (siProprio) {
            int id = ClientHandler.clientHandlers.get(gameOnGoing.posicaoJogadorAtual(true)).getPlayer().getId();
            int quantCartas = ClientHandler.clientHandlers.get(gameOnGoing.posicaoJogadorAtual(true)).getPlayer().getDeck().size();
            ClientHandler clientHandler = (ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])));
            clientHandler.toAClient("10\t" + id + "\t" + quantCartas + "\n", Integer.parseInt(clientMessage[0]));
            return;
        }
        for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
            if (!ClientHandler.clientHandlers.get(i).equals(ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])))) {
                int id = ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])).getPlayer().getId();
                int quantCartas = ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])).getPlayer().getDeck().size();
                ClientHandler.clientHandlers.get(i).toAClient("10\t" + id + "\t" + quantCartas + "\n", i);
            }
        }
    }

    public static void enviaGritouUno(String[] clientMessage, boolean unoParaSi) {
        if (unoParaSi) {
            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                if (!ClientHandler.clientHandlers.get(i).equals(ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])))) {
                    int id = ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])).getPlayer().getId();
                    boolean gritouUno = ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])).getPlayer().isGritouUno();
                    ClientHandler.clientHandlers.get(i).toAClient("11\t" + id + "\t" + gritouUno + "\n", i);
                }
            }
            return;
        }
        int i = 0;
        while (i < ClientHandler.clientHandlers.size()) {
            if (ClientHandler.clientHandlers.get(i).getPlayer().getDeck().size() == 1) {
                break;
            }
            i++;
        }
        i--;
        boolean gritouUno = oponenteGritouUno;
        ClientHandler clientHandler = (ClientHandler.clientHandlers.get(i));
        clientHandler.toAClient("12\t" + gritouUno + "\n", i);
    }
}
