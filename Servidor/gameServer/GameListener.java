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

    public GameListener() {
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
                System.out.println("\u001B[32m" + "msg: " + newMessages.peek() + "\u001B[0m");
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
                                    gameOnGoing.pescaCarta(i);
                                }

                            }

                            try {
                                Server.temNovaJogadaSemaphore.acquire();
                            } catch (InterruptedException e) {
                                System.out.println("Sempahore Exception!");
                            }
                            //Envia para os oponentes a quantidade de cartas atualizada
                            enviaQuantCartas(clientMessage, false);
                            gameTemNovJogada = true;
                            Server.temNovaJogadaSemaphore.release();
                            break;
                        //Jogar carta  normal
                        case 5:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    gameOnGoing.setCartaNaMesa(ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                                    gameOnGoing.enviaCartaNaMesa(i);
                                    ClientHandler.clientHandlers.get(i).getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));
                                }
                            }
                            enviaQuantCartas(clientMessage, false);
                            try {
                                Server.temNovaJogadaSemaphore.acquire();
                            } catch (InterruptedException e) {
                                System.out.println("Sempahore Exception!");
                            }
                            gameTemNovJogada = true;
                            Server.temNovaJogadaSemaphore.release();
                            break;
                        //Jogar carta  especial
                        case 6:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    //Realiza a ação da carta especial
                                    if (("IN").equals(((CartaEspecial) ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial())) {
                                        gameOnGoing.setOrdemParaDireita(!gameOnGoing.isOrdemParaDireita());
                                    }
                                    if (((CartaEspecial) ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial().equals("+2")) {
                                        gameOnGoing.setPosicaoAtual(gameOnGoing.posicaoJogadorAtual());
                                        gameOnGoing.pescaCarta(gameOnGoing.posicaoJogadorAtual());
                                        gameOnGoing.pescaCarta(gameOnGoing.posicaoJogadorAtual());
                                    }
                                    if (((CartaEspecial) ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial().equals("+4")) {
                                        gameOnGoing.setPosicaoAtual(gameOnGoing.posicaoJogadorAtual());
                                        gameOnGoing.pescaCarta(gameOnGoing.posicaoJogadorAtual());
                                        gameOnGoing.pescaCarta(gameOnGoing.posicaoJogadorAtual());
                                        gameOnGoing.pescaCarta(gameOnGoing.posicaoJogadorAtual());
                                        gameOnGoing.pescaCarta(gameOnGoing.posicaoJogadorAtual());
                                    }
                                    if (((CartaEspecial) ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2]))).getTipoEspecial().equals("PJ")) {
                                        gameOnGoing.setPosicaoAtual(gameOnGoing.posicaoJogadorAtual());
                                    }
                                    //Seta carta na Mesa, e caso for uma carta de escolher cor altera sua cor também
                                    if (clientMessage.length == 4) {
                                        ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])).setCor(CartaEspecial.getCor(clientMessage[3]));
                                    }
                                    gameOnGoing.setCartaNaMesa(ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                                    gameOnGoing.enviaCartaNaMesa(i);
                                    ClientHandler.clientHandlers.get(i).getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));

                                    //Atualiza quantidade de cartas dos oponenetes
                                    enviaQuantCartas(clientMessage, true);

                                }
                            }
                            try {
                                Server.temNovaJogadaSemaphore.acquire();
                            } catch (InterruptedException e) {
                                System.out.println("Sempahore Exception!");
                            }
                            gameTemNovJogada = true;
                            Server.temNovaJogadaSemaphore.release();
                            break;
                    }
                }

            }
            // Libera o semaphore após acessar as condições de corrida
            Server.mensagensSemaphore.release();
        }
    }

    private void enviaQuantCartas(String[] clientMessage, boolean siProprio) {
        int id = ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])).getPlayer().getId();
        int quantCartas = ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])).getPlayer().getDeck().size();

        if(siProprio){
            ClientHandler clientHandler = (ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])));
            clientHandler.toAClient("10\t" + id + "\t" + quantCartas + "\n", Integer.parseInt(clientMessage[0]));
            return;
        }
        for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
            if (!ClientHandler.clientHandlers.get(i).equals(ClientHandler.clientHandlers.get(Integer.parseInt(clientMessage[0])))) {
                ClientHandler.clientHandlers.get(i).toAClient("10\t" + id + "\t" + quantCartas + "\n", i);
            }
        }
    }
}
