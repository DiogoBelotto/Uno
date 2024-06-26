package game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import players.Player;

public class Client {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private Player player;
    private final Queue<String> mensagensRecebidas;

    public Client(Socket socket) {
        mensagensRecebidas = new LinkedList<>();
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            player = new Player();
        } catch (IOException e) {
            closeTudo(bufferedReader, bufferedWriter, socket);
        }

    }

    public void enviaMensagem(String mensagem, int i) {// Header de mensagem (int no inicio): 1 igual a nome, 2 igual a
        // estado de pronto do player,
        try {
            switch (i) {
                case 1:
                    player.setNome(mensagem);
                    bufferedWriter.write("01\t" + player.getNome() + "\n");
                    break;
                case 3:
                    bufferedWriter.write("03\t" + player.isPronto() + "\n");
                    break;
                case 4:
                    bufferedWriter.write("04\t" + "\n");
                    break;
                case 5:
                    bufferedWriter.write(mensagem);
            }

            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeTudo(bufferedReader, bufferedWriter, socket);
        }
    }

    public void closeTudo(BufferedReader bufferedReader, BufferedWriter bufferedWriter, Socket socket) {
        try {
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
            if (socket != null)
                socket.close();
            System.out.println("\u001B[31m" + "\nPerda de conexão com o Servidor! Partida encerrada!" + "\u001B[0m");
        } catch (IOException e) {
            System.out.println("Close Exception! ");
        }
    }

    public void escutaMensagem() {
        new Thread(() -> {
            String mensagem = null;
            while (socket.isConnected()) {
                try {
                    mensagem = bufferedReader.readLine();
                    mensagensRecebidas.add(mensagem);

                } catch (IOException e) {
                    closeTudo(bufferedReader, bufferedWriter, socket);
                    break;
                }
            }
        }).start();

    }

    public Player getPlayer() {
        return player;
    }

    public Queue<String> getMensagensRecebidas() {
        return mensagensRecebidas;
    }
}
