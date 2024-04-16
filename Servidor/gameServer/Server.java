package gameServer;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Server {
    private final ServerSocket serverSocket;
    private final GameListener gameListener;
    public static Semaphore mensagensSemaphore;
    public static Semaphore numPlayersSemaphore;
    public static Semaphore playersSemaphore;
    public static Semaphore temNovaJogadaSemaphore;
    public static Semaphore geralSemaphore;

    public Server(ServerSocket serverSocket) throws IOException {
        mensagensSemaphore = new Semaphore(1);
        numPlayersSemaphore = new Semaphore(1);
        playersSemaphore = new Semaphore(1);
        temNovaJogadaSemaphore = new Semaphore(1);
        geralSemaphore = new Semaphore(1);

        gameListener = new GameListener();
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        ClientHandler.setTemNovaMensagem(false);
        ClientHandler.setNovasMensagens(new LinkedList<>());
        Thread thread = new Thread(gameListener);
        thread.start();
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                // Verificar se ja há 4 players e aceita o socket caso haja menos
                try {
                    Server.numPlayersSemaphore.acquire();
                } catch (InterruptedException e) {
                    System.out.println("Sempahore Exception!");
                }
                if (GameListener.numPlayers > 3) {
                    //Caso haja 4 players envia uma mensagem com identificador 02 antes de fechar o socket
                    bufferedWriter.write("02\t\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    socket.close();
                } else {
                    //Caso seja aceito o socket envia mensagem com identificador 03
                    bufferedWriter.write("03\t\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("Conexao com cliente Realizada!");
                    GameListener.numPlayers++;
                    ClientHandler clienteHandler = new ClientHandler(socket);

                    Thread thread1 = new Thread(clienteHandler);
                    thread1.start();
                }
                Server.numPlayersSemaphore.release();

            }
        } catch (IOException e) {
            System.out.println("Critic Exception!");
        }

    }
}
