package gameServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;

import players.Player;

public class ClientHandler implements Runnable {
    // Lista de todos os clientHandlers para acessar estaticamente fora das Threads
    // criadas
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();
    // Variaveis estaticas usadas para passar as mensagem escutadas pelos
    // clientHandlers para outras classes
    public static boolean temNovaMensagem;
    public static Queue<String> novasMensagens;

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Player player;
    // Id do ClientHandler vai ser o mesmo do Player
    private int id;


    public ClientHandler(Socket socket) {
        try {
            // Inicializa os buffers leitores e escritores, e o socket
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Defini um id único para cada ClientHandler
            if (clientHandlers.isEmpty() || clientHandlers.getLast() == null)
                this.id = 0;
            else
                this.id = clientHandlers.getLast().getId() + 1;
            clientHandlers.add(this);

        } catch (IOException e) {
            closeTudo(bufferedReader, bufferedWriter, socket);
        }

    }

    @Override
    public void run() {
        // Loop que vai ficar escutando o socket aceito
        String mensagem = null;
        while (socket.isConnected()) {
            try {
                if (!temNovaMensagem) {
                    // Utilizando um semaphore para manipular as variaveis estaticas usadas para
                    // passar as mensagem para classes externa

                    mensagem = bufferedReader.readLine();

                    Server.mensagensSemaphore.acquire();
                    //Adiciona a mensagem a variavel junto ao id do clientHandler em questão para identificação externa
                    novasMensagens.add(this.id + "\t" + mensagem);
                    //DebugMSG - System.out.println(novaMensagem);
                    temNovaMensagem = true;
                    Server.mensagensSemaphore.release();
                }
            } catch (IOException e) {
                closeTudo(bufferedReader, bufferedWriter, socket);
                break;
            } catch (InterruptedException e) {
                System.out.println("Exeception semphore");
            }
        }
    }

    public void toAllClient(String message) {
        //Para enviar mensagem a todos os palyers (Broadcast)
        System.out.print("\u001B[35m" + "Mensagem de saida para Todos os Clientes: " + message + "\u001B[0m");

        for (ClientHandler clienteHandler : clientHandlers) {
            try {
                clienteHandler.bufferedWriter.write(message);
                clienteHandler.bufferedWriter.newLine();
                clienteHandler.bufferedWriter.flush();

            } catch (IOException e) {
                closeTudo(bufferedReader, bufferedWriter, socket);
            }
        }
    }

    public void toAClient(String message, int i) {
        //Para enviar mensagem para somente um player usando um identificar (i) (encontrado externamente ao método)
        try {
            ClientHandler.getClientHandlers().get(i).getBufferedWriter().write(message);
            ClientHandler.getClientHandlers().get(i).getBufferedWriter().newLine();
            ClientHandler.getClientHandlers().get(i).getBufferedWriter().flush();
            System.out.print("\u001B[34m" + "Mensagem de saida para o cliente de Id - " + ClientHandler.getClientHandlers().get(i).getPlayer().getId() + ": " + message + "\u001B[0m");
        } catch (IOException e) {
            System.out.println("Single sent msg Execption");
        }

    }

    public void removeClientHandler() {
        //remove o client da lista caso ele saia ou seja removido por outro motivo
        clientHandlers.remove(this);
        try {
            Server.mensagensSemaphore.acquire();
        } catch (InterruptedException e) {
            System.out.println("Exeception semphore");
        }
        //Adiciona nova mensagem para o GameListener tratar, com o id do clientHandler e Identificador 2
        novasMensagens.add(this.id + "\t2");
        temNovaMensagem = true;
        Server.mensagensSemaphore.release();
    }

    public void closeTudo(BufferedReader bufferedReader, BufferedWriter bufferedWriter, Socket socket) {
        removeClientHandler();
        try {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public int getId() {
        return id;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public static void setTemNovaMensagem(boolean temNovaMensagem) {
        ClientHandler.temNovaMensagem = temNovaMensagem;
    }

    public static ArrayList<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    public static void setNovasMensagens(Queue<String> novasMensagens) {
        ClientHandler.novasMensagens = novasMensagens;
    }

    //Encontra o clientHandler por seu ID e retorna sua Posição na lista de ClientHandlers
    public static int getByID(int i){
        for(int j=0; j< clientHandlers.size(); j++){
            if(clientHandlers.get(j).getId() == i)
                return j;
        }
        return -1;
    }
}
