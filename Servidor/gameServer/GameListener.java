package gameServer;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import baralho.CartaEspecial;
import players.Player;

//Classe responsável por Tratar as mensagem recebida pelos sockets do ClientHandler
public class GameListener implements Runnable {

    public static int numPlayers;
    private boolean jogoIniciado;
    private int totalProntos;
    private final GameOnGoing gameOnGoing;
    public static LinkedList<Player> players;

    public GameListener() {
        gameOnGoing = new GameOnGoing();
        players = new LinkedList<>();
        numPlayers = 0;
        jogoIniciado = false;
    }

    @Override
    public void run() {
        // Semaphore para critic sessions
        Semaphore semaphore = new Semaphore(1);

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
                semaphore.acquire();
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

                                    players.add(player);

                                    System.out.println(player.getNome());
                                    System.out.println("player ENTROU! total de palyers: " + numPlayers);
                                    ClientHandler.clientHandlers.getFirst().toAllClient("01\t" + numPlayers + "\n");
                                }

                            }
                            break;

                        // Identificador 2: Saída de Jogador
                        case 2:
                            for (int i = 0; i < players.size(); i++) {
                                if (players.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    if (players.get(i).isPronto())
                                        totalProntos--;
                                    players.remove(i);
                                }
                            }
                            numPlayers--;
                            if (ClientHandler.clientHandlers != null && ClientHandler.clientHandlers.getFirst() != null)
                                if (!ClientHandler.clientHandlers.isEmpty())
                                    ClientHandler.clientHandlers.getFirst().toAllClient("01\t" + numPlayers + "\n");
                            System.out.println("player SAIU! total de players: " + numPlayers);
                            break;

                        // Identificador 3: Jogador deu pronto
                        case 3:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    boolean isPronto = clientMessage[2].equals("true");
                                    if (isPronto) {
                                        ClientHandler.clientHandlers.get(i).getPlayer().setPronto(true);
                                        totalProntos++;
                                        System.out.println(
                                                "Player " + ClientHandler.clientHandlers.get(i).getPlayer().getNome()
                                                        + " está pronto! \nFaltam " + (numPlayers - totalProntos)
                                                        + " players darem pronto para o jogo iniciar!");
                                        System.out.println(
                                                (numPlayers == 1) ? "Mas nao é possivel jogar sozinho!" : "\r");
                                    }

                                }

                            }
                            break;
                        case 4:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    gameOnGoing.pescaCarta(i);
                                }
                            }
                            break;
                        case 5:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    gameOnGoing.setCartaNaMesa(ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                                    gameOnGoing.enviaCartaNaMesa(i);
                                    ClientHandler.clientHandlers.get(i).getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));
                                }
                            }
                            break;
                        case 6:
                            for (int i = 0; i < ClientHandler.clientHandlers.size(); i++) {
                                if (ClientHandler.clientHandlers.get(i).getId() == Integer.parseInt(clientMessage[0])) {
                                    if (clientMessage.length == 4) {
                                        gameOnGoing.setCartaNaMesa(ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                                        gameOnGoing.getCartaNaMesa().setCor(CartaEspecial.getCor(clientMessage[3]));
                                        gameOnGoing.enviaCartaNaMesa(i);
                                        ClientHandler.clientHandlers.get(i).getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));
                                    } else {
                                        gameOnGoing.setCartaNaMesa(ClientHandler.clientHandlers.get(i).getPlayer().getDeck().get(Integer.parseInt(clientMessage[2])));
                                        gameOnGoing.enviaCartaNaMesa(i);
                                        ClientHandler.clientHandlers.get(i).getPlayer().getDeck().remove(Integer.parseInt(clientMessage[2]));
                                    }
                                }
                            }
                            break;
                    }
                }

            }
            // Libera o semaphore após acessar as condições de corrida
            semaphore.release();
        }
    }
}
