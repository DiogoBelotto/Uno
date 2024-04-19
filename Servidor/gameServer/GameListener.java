package gameServer;

import java.util.LinkedList;
import java.util.Objects;
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

        // Loop que verifica a existencia de mensagem escitas pelo ClienteHandler na variavel estatica novaMensagem e temNovaMensage
        Queue<String> newMessages = new LinkedList<>();
        while (true) {
            // Caso o jogo ainda não foi iniciado e existam no minimo dois players com status pronto inicia a uma nova thread
            // na Clase GameOnGoing responsável por controlar a lógica do jogo
            if (!jogoIniciado)
                if (numPlayers > 1 && totalProntos == numPlayers) {
                    jogoIniciado = true;
                    Thread th1 = new Thread(gameOnGoing);
                    th1.start();
                }
            // Requesita o semaphore antes de entrar no switch e finaliza ao seu final visto que acessam e modificam muitas vezes as variaveis em seção critica
            try {
                Thread.sleep(60);
                Server.mensagensSemaphore.acquire();
            } catch (InterruptedException e) {
                System.out.println("Sempahore Exception!");
            }


            // Verifica se há alguma mensagem nova escutada pelos sockets
            if (ClientHandler.temNovaMensagem && ClientHandler.novasMensagens.peek() != null
                    && !ClientHandler.novasMensagens.peek().isEmpty()) {
                // Divide a mensagem em um array de Strings, a posição 0 é o id do ClientHandler/Player de Origem, a posição 1 é o identificador do tipo de ação
                // a posição 2 a mensagem em si, no caso de uma carta com opção de escolher cor há uma posição 3
                for (int i = 0; i < ClientHandler.novasMensagens.size(); i++) {
                    newMessages.add(ClientHandler.novasMensagens.poll());
                }

                //Printa todas as mensagens de entrada
                System.out.println("\u001B[32m" + "Mensagem de entrada: " + newMessages.peek() + "\u001B[0m");
                String[] clientMessage = Objects.requireNonNull(newMessages.poll()).split("\t");

                Player player;
                int j;
                ClientHandler clientHandler;

                if (newMessages.isEmpty())
                    ClientHandler.temNovaMensagem = false;

                if (clientMessage.length > 1) {

                    switch (Integer.parseInt(clientMessage[1])) {
                        // Identificador 1: Criar Jogador com nome recebido por mensagem
                        case 1:
                            //Encontra o clientHandler por seu ID
                            j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
                            clientHandler = ClientHandler.clientHandlers.get(j);

                            clientHandler.setPlayer(new Player(clientMessage[2]));
                            player = clientHandler.getPlayer();
                            player.setId(clientHandler.getId());

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


                            break;

                        // Identificador 2: Saída de Jogador
                        case 2:
                            try {
                                Server.numPlayersSemaphore.acquire();
                                Server.playersSemaphore.acquire();
                            } catch (InterruptedException e) {
                                System.out.println("Sempahore Exception!");
                            }

                            //Encontra o clientHandler por seu ID
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
                            //Encontra o clientHandler por seu ID
                            j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
                            clientHandler = ClientHandler.clientHandlers.get(j);

                            boolean isPronto = clientMessage[2].equals("true");
                            if (isPronto) {
                                try {
                                    Server.numPlayersSemaphore.acquire();
                                } catch (InterruptedException e) {
                                    System.out.println("Sempahore Exception!");
                                }
                                clientHandler.getPlayer().setPronto(true);
                                totalProntos++;
                                System.out.println("Player " + clientHandler.getPlayer().getNome()
                                        + " está pronto! \nFaltam " + (numPlayers - totalProntos)
                                        + " players darem pronto para o jogo iniciar!");
                                System.out.println((numPlayers == 1) ? "Mas nao é possivel jogar sozinho!" : "\r");
                                Server.numPlayersSemaphore.release();
                            }
                            break;

                        //Pescar carta
                        case 4:
                            //Encontra o clientHandler por seu ID
                            j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
                            clientHandler = ClientHandler.clientHandlers.get(j);

                            if (clientHandler.getPlayer().getDeck().size() == 1)
                                clientHandler.getPlayer().setGritouUno(false);
                            gameOnGoing.pescaCarta(j);

                            //Envia para os oponentes a quantidade de cartas atualizada
                            enviaQuantCartas(clientMessage, false);
                            synchronized (gameOnGoing) {
                                gameOnGoing.notify();
                            }
                            break;
                        //Jogar carta  normal
                        case 5:
                            //Encontra o clientHandler por seu ID
                            j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
                            clientHandler = ClientHandler.clientHandlers.get(j);

                            gameOnGoing.setCartaNaMesa(clientHandler.getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                            gameOnGoing.enviaCartaNaMesa(j);
                            clientHandler.getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));

                            enviaQuantCartas(clientMessage, false);

                            synchronized (gameOnGoing) {
                                gameOnGoing.notify();
                            }

                            break;
                        //Jogar carta  especial
                        case 6:
                            //Encontra o clientHandler por seu ID
                            j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
                            clientHandler = ClientHandler.clientHandlers.get(j);

                            Player playerDaRodada;

                            //Realiza a ação da carta especial
                            playerDaRodada = clientHandler.getPlayer();
                            if (("IN").equals(((CartaEspecial) playerDaRodada.getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial())) {
                                gameOnGoing.setPosicaoAtual(gameOnGoing.posicaoJogadorAtual(true));
                                gameOnGoing.setPosicaoAtual(gameOnGoing.posicaoJogadorAtual(true));
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


                            //Seta carta na Mesa, e caso for uma carta de escolher cor altera sua cor também
                            if (clientMessage.length == 4) {
                                clientHandler.getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])).setCor(CartaEspecial.getCor(clientMessage[3]));
                            }
                            gameOnGoing.setCartaNaMesa(clientHandler.getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                            gameOnGoing.enviaCartaNaMesa(j);
                            boolean isMudaCor = false;
                            if (playerDaRodada != null) {
                                isMudaCor = ((CartaEspecial) playerDaRodada.getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial().equals("MC");
                            }

                            clientHandler.getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));
                            //Atualiza quantidade de cartas dos oponenetes e para si mesmo
                            if (!isMudaCor)
                                enviaQuantCartas(clientMessage, true);
                            enviaQuantCartas(clientMessage, false);
                            String[] idJogadorQuePescou = {String.valueOf(gameOnGoing.posicaoJogadorAtual(true))};
                            enviaQuantCartas(idJogadorQuePescou, false);

                            //Envia aos players a informação de que há uma nova rodada, apenas se for uma carta especial do tipo +2, +4 ou PJ, que vão pular um jogador
                            if (playerDaRodada != null) {
                                jogadaCartaEspecial = true;
                                //Comunica o player que é sua rodada
                                ClientHandler.clientHandlers.get(gameOnGoing.getPosicaoAtual()).toAClient("08\t1\n", gameOnGoing.getPosicaoAtual());
                                //Envia aos demais players que não é sua rodada
                                for (int k = 0; k < GameListener.players.size(); k++) {
                                    if (k != gameOnGoing.getPosicaoAtual()) {
                                        ClientHandler.clientHandlers.get(k).toAClient("08\t0\n", k);
                                    }
                                }
                            }

                            synchronized (gameOnGoing) {
                                gameOnGoing.notify();
                            }
                            break;
                        //Grita uno para si mesmo
                        case 7:
                            //Encontra o clientHandler por seu ID
                            j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
                            clientHandler = ClientHandler.clientHandlers.get(j);

                            //Caso nenhum oponente tenha gritado uno antes
                            if (!oponenteGritouUno) {
                                clientHandler.getPlayer().setGritouUno(true);
                                enviaGritouUno(clientMessage, true);
                            }
                            break;
                        //Grita uno para oponente
                        case 8:
                            //Caso o jogador não tenha gritado uno antes
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getPlayer().getDeck().size() == 1) {
                                    if (!ClientHandler.clientHandlers.get(i).getPlayer().isGritouUno()) {
                                        oponenteGritouUno = true;
                                        ClientHandler.clientHandlers.get(i).toAllClient("14\t" + true + "\n");
                                        gameOnGoing.pescaCarta(i);
                                        gameOnGoing.pescaCarta(i);
                                        //Enviar para o jogador que o oponente gritou uno
                                        enviaGritouUno(clientMessage, false);
                                    }
                                }
                            }

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
            int j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
            ClientHandler clientHandler = (ClientHandler.clientHandlers.get(j));
            clientHandler.toAClient("10\t" + id + "\t" + quantCartas + "\n", Integer.parseInt(clientMessage[0]));
            return;
        }

        int j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
        ClientHandler clientHandler = ClientHandler.clientHandlers.get(j);
        int id = clientHandler.getId();
        int quantCartas = clientHandler.getPlayer().getDeck().size();
        for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
            if (!(ClientHandler.clientHandlers.get(i).equals(clientHandler))) {
                ClientHandler.clientHandlers.get(i).toAClient("10\t" + id + "\t" + quantCartas + "\n", i);
            }
        }
    }

    public static void enviaGritouUno(String[] clientMessage, boolean unoParaSi) {
        if (unoParaSi) {
            int j = ClientHandler.getByID(Integer.parseInt(clientMessage[0]));
            ClientHandler clientHandler = ClientHandler.clientHandlers.get(j);
            int id = clientHandler.getId();
            boolean gritouUno = clientHandler.getPlayer().isGritouUno();
            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                if (!ClientHandler.clientHandlers.get(i).equals(clientHandler)) {
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
