package game;

import java.util.Scanner;

public class PerguntaPlayer implements Runnable {
    private Scanner scanner;
    private final Screen screen;
    public String mensagem;

    public PerguntaPlayer(Screen screen) {
        scanner = new Scanner(System.in);
        this.screen = screen;
    }

    public void start() {
        mensagem = scanner.nextLine();
        synchronized (screen) {
            screen.notify();
        }
    }

    public void close() {
        synchronized (screen) {
            screen.notify();
        }
        scanner.close();
    }

    @Override
    public void run() {
        while(true){
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            mensagem = null;
            start();
        }

    }

    public void newScanner(){
        scanner = new Scanner(System.in);
    }
}
