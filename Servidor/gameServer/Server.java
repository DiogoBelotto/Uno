package gameServer;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server {
    private ServerSocket serverSocket;
    private GameListener gameListener;

    public Server(ServerSocket serverSocket) throws IOException {
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

            }
        } catch (IOException e) {
            System.out.println("Critic Exception!");
        }

    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
