package game;

import baralho.Carta;
import baralho.CartaEspecial;
import baralho.CartaNormal;
import baralho.TipoEspecial;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Screen {
    private final Game game;
    public static int tipoDePrint;
    public static Semaphore mensagensSemaphore;
    public static Semaphore tipoDePrintSemaphore;
    public static Semaphore podeComecarSemaphore;
    public static Semaphore rodadaAtualSemaphore;
    public static Semaphore novaRodadaSemaphore;
    public static Semaphore numPlayersSemaphore;
    public static Semaphore geralSemaphore;
    private Thread th1;
    private final PerguntaPlayer perguntaPlayer;


    public Screen() throws IOException {
        perguntaPlayer = new PerguntaPlayer(this);
        th1 = new Thread(perguntaPlayer);
        th1.start();
        mensagensSemaphore = new Semaphore(1);
        tipoDePrintSemaphore = new Semaphore(1);
        podeComecarSemaphore = new Semaphore(1);
        rodadaAtualSemaphore = new Semaphore(1);
        novaRodadaSemaphore = new Semaphore(1);
        numPlayersSemaphore = new Semaphore(1);
        geralSemaphore = new Semaphore(1);
        tipoDePrint = 0;
        game = new Game(this);
    }

    public void startScreen() {
        // Inicia a logica do Game em outra Thread para separar essa apenas para
        // impressão
        Thread theared = new Thread(game);
        theared.start();

        // loop que printa a tela do jogo
        do {
            try {
                tipoDePrintSemaphore.acquire();
            } catch (InterruptedException e) {
                System.out.println("Semaphore Exception");
            }
            boolean var = (tipoDePrint != 0);
            tipoDePrintSemaphore.release();

            // Se o tipoDePrint for diferente de 0 começa a printar o jogo
            if (var) {
                switch (tipoDePrint) {
                    // Identificador 1: Tela do Jogo
                    case 1:
                        preGame();
                        tipoDePrint = 4;
                        break;
                    // Identificador 2: Partida Lotada, numero max de jogadores
                    case 2:
                        System.out.println("Número máximo de jogadores ja entraram!");
                        //Thread.interrupted();
                        break;
                    case 3:
                        System.out.println("\u001B[32m" + "=======Jogo terminou!=======" + "\u001B[0m");
                        return;

                }
            }
        } while (true);
    }

    public void preGame() {

        // Pede o nome do usuario para criar o seu Player e envia o nome por socket
        System.out.print("Digite seu username: ");

        //Libera a Thread de leitura do Usuario
        synchronized (perguntaPlayer){
            perguntaPlayer.notify();
        }
        //Aguarda a Thread de leitura do Usuario
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                System.out.println("\nExecption wait! ");
            }
        }
        game.getClient().enviaMensagem(perguntaPlayer.mensagem, 1);

        String resposta = null;
        // Loop para aguardar o jogo iniciar
        while (true) {
            try {
                podeComecarSemaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (game.isPodeComecar()) {
                podeComecarSemaphore.release();
                break;
            }
            podeComecarSemaphore.release();

            if (!game.getClient().getPlayer().isPronto()) {
                System.out.print("Aguardando players ficarem prontos! Se você está pronto digite 'S' ");

                //Libera a Thread de leitura do Usuario
                synchronized (perguntaPlayer){
                    perguntaPlayer.notify();
                }
                //Aguarda a Thread de leitura do Usuario
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        System.out.println("\nExecption wait! ");
                    }
                }
                resposta = perguntaPlayer.mensagem;
            }

            assert resposta != null;
            if (resposta.equalsIgnoreCase("S")) {
                game.getClient().getPlayer().setPronto(true);
                game.getClient().enviaMensagem(null, 3);
                resposta = "";
            }
        }

        // Quando podeComecar entra nesse loop
        while (!game.getOpontentes().isEmpty()) {
            // Caso os players tenham desconectado, encerra o Jogo
            // Layout da impressão deve mudar de acordo com a quantidade de players
            try {
                geralSemaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            switch (game.getNumPlayers()) {
                case 2:

                    System.out.print("\033[H\033[2J" + "\u001B[32m" + "Dois players\n");
                    System.out.print("[IN]Inverte Ordem, [PJ]Pula Jogador, [MC]Muda Cor\n");
                    System.out.print("\033[H\033[2J" + "\r------" + game.getOpontentes().getFirst().getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas");
                    System.out.print("\n\n---------" + game.getCartaNaMesa());
                    System.out.print("\n\n------" + game.getClient().getPlayer().getDeck() + "\u001B[0m");
                    geralSemaphore.release();
                    isSuaRodada();
                    break;

                case 3:
                    System.out.print("\033[H\033[2J" + "\u001B[32m" + "Tres players\n");
                    System.out.print("\u001B[32m" + "[IN]Inverte Ordem, [PJ]Pula Jogador, [MC]Muda Cor\n");
                    System.out.print("\033[H\033[2J" + "\r------" + game.getOpontentes().getFirst().getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas");
                    System.out.print("\n------" + game.getOpontentes().get(1).getNome() + " ["
                            + game.getOpontentes().get(1).getQuantCartas() + "] cartas");
                    System.out.print("\n\n---------" + game.getCartaNaMesa());
                    System.out.print("\n\n------" + game.getClient().getPlayer().getDeck() + "\u001B[0m");
                    geralSemaphore.release();
                    isSuaRodada();
                    break;

                case 4:
                    System.out.print("\033[H\033[2J" + "\u001B[32m" + "\u001B[32m" + "Quatro players\n");
                    System.out.print("[IN]Inverte Ordem, [PJ]Pula Jogador, [MC]Muda Cor\n");
                    System.out.print("\033[H\033[2J" + "\r------" + game.getOpontentes().getFirst().getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas");
                    System.out.print("\n------" + game.getOpontentes().get(1).getNome() + " ["
                            + game.getOpontentes().get(1).getQuantCartas() + "] cartas");
                    System.out.print("\n------" + game.getOpontentes().get(2).getNome() + " ["
                            + game.getOpontentes().get(2).getQuantCartas() + "] cartas");
                    System.out.print("\n\n---------" + game.getCartaNaMesa());
                    System.out.print("\n\n------" + game.getClient().getPlayer().getDeck() + "\u001B[0m");
                    geralSemaphore.release();
                    isSuaRodada();
                    break;


            }

            if (!game.isSuaRodada())
                System.out.println("\nAguardando a rodada de outro Jogador! ");

            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    System.out.println("\nExecption wait! ");
                }
            }
            if(tipoDePrint !=1)
                break;
        }
    }

    private void isSuaRodada() {
        if (game.isSuaRodada()) {
            boolean isJogadaValida;
            do {
                System.out.print("\u001B[32m" + "\n--Seu Turno--" + "\u001B[0m");
                System.out.print("\u001B[32m" + "\n--Indique a posição da carta para jogar (de 1 até " + game.getClient().getPlayer().getDeck().size() + "):\u001B[0m");
                System.out.print("\u001B[32m" + "\n--Ou insira -1 para pescar carta--" + "\u001B[0m");
                if (game.getClient().getPlayer().getDeck().size() == 2)
                    System.out.print("\u001B[34m" + "\n--Insira um 'espaço' e -2 no final da jogada para GRITAR UNO ao jogar \n(Ex:'2 -2' (Carta num. 2 gritando uno))--" + "\u001B[0m");
                for (int i = 0; i < game.getOpontentes().size(); i++) {
                    if (!game.getOpontentes().get(i).isGritouUnoParasi() && game.getOpontentes().get(i).getQuantCartas() == 1)
                        System.out.print("\u001B[34m" + "\n--Insira um 'espaço' e -3 para GRITAR UNO PARA " + game.getOpontentes().get(i).getNome() + "\n(Ex:'2 -2' (Carta num. 2 gritando uno))-- \u001B[0m");
                }

                boolean entradaValida = false;
                //Libera a Thread de leitura do Usuario
                synchronized (perguntaPlayer){
                    perguntaPlayer.notify();
                }
                //Aguarda a Thread de leitura do Usuario
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        System.out.println("\nExecption wait! ");
                    }
                }
                String jogada = perguntaPlayer.mensagem;
                int[] escolha = {0, 0};
                if(jogada == null)
                    return;

                while (!entradaValida) {
                    try {
                        String[] jogadaTemp = jogada.split(" ");
                        escolha[0] = Integer.parseInt(jogadaTemp[0]);
                        if (jogadaTemp.length == 2)
                            escolha[1] = Integer.parseInt(jogadaTemp[1]);
                        entradaValida = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Jogada inválida! Tente novamente: ");
                        //Libera a Thread de leitura do Usuario
                        synchronized (perguntaPlayer){
                            perguntaPlayer.notify();
                        }
                        //Aguarda a Thread de leitura do Usuario
                        synchronized (this) {
                            try {
                                this.wait();
                            } catch (InterruptedException ex) {
                                System.out.println("\nExecption wait! ");
                            }
                        }
                        jogada = perguntaPlayer.mensagem;
                        if(jogada == null)
                            return;
                    }
                }

                String jogadaTraduzidaParaEnvio = verificaJogada(escolha);

                LinkedList<String> jogadasTraduzidasParaEnvio = new LinkedList<>();
                jogadasTraduzidasParaEnvio.add(jogadaTraduzidaParaEnvio);

                isJogadaValida = !jogadaTraduzidaParaEnvio.equals("err01jogada-invalida01\t");

                if (jogadaTraduzidaParaEnvio.equals("7\t") || jogadaTraduzidaParaEnvio.equals("8\t")) {
                    escolha = new int[]{escolha[0]};
                    jogadaTraduzidaParaEnvio = verificaJogada(escolha);
                    jogadasTraduzidasParaEnvio.add(jogadaTraduzidaParaEnvio);
                    isJogadaValida = !jogadaTraduzidaParaEnvio.equals("err01jogada-invalida01\t");
                }
                if (isJogadaValida) {
                    if (jogadasTraduzidasParaEnvio.size() == 2) {
                        game.getClient().enviaMensagem(jogadasTraduzidasParaEnvio.get(0), 5);
                        game.getClient().enviaMensagem(jogadasTraduzidasParaEnvio.get(1), 5);
                    } else game.getClient().enviaMensagem(jogadasTraduzidasParaEnvio.get(0), 5);
                } else System.out.print("Jogada inválida! ");

                jogadasTraduzidasParaEnvio.clear();
            } while (!isJogadaValida);
        }
        game.setSuaRodada(false);
    }

    public String verificaJogada(int[] jogada) {

        //Caso for jogada de gritar uno
        if (jogada.length == 2) {
            //gritar uno
            if (jogada[1] == -2) {
                if ((game.getClient().getPlayer().getDeck().size() == 1 || game.getClient().getPlayer().getDeck().size() == 2) && !game.isOponenteGritouUNo())
                    return "7\t";
                return "err01jogada-invalida01\t";
            }
            //gritar uno para outro jogador
            if (jogada[1] == -3) {
                for (int i = 0; i < game.getOpontentes().size(); i++) {
                    if (!game.getOpontentes().get(i).isGritouUnoParasi() && game.getOpontentes().get(i).getQuantCartas() == 1)
                        return "8\t";
                }
                return "err01jogada-invalida01\t";
            }
        }

        //pescar carta
        if (jogada[0] == -1) {
            return "4\t";
        }

        //jogar cartas
        if (jogada[0] > 0 && jogada[0] < (game.getClient().getPlayer().getDeck().size()) + 1) {
            Carta carta = game.getClient().getPlayer().getDeck().get(jogada[0] - 1);
            if (carta instanceof CartaEspecial) {
                //Caso for um +4 ou Muda Cor
                if (((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.MAIS_4) ||
                        ((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.MUDA_COR)) {
                    game.getClient().getPlayer().getDeck().remove(jogada[0] - 1);

                    //Pede a cor desejada
                    boolean respostaValida = false;
                    System.out.println("Qual a cor desejada? (azul, amarelo, vermelho, verde)");
                    //Libera a Thread de leitura do Usuario
                    synchronized (perguntaPlayer){
                        perguntaPlayer.notify();
                    }
                    //Aguarda a Thread de leitura do Usuario
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            System.out.println("\nExecption wait! ");
                        }
                    }
                    String resposta = perguntaPlayer.mensagem;

                    do {
                        if (resposta.equalsIgnoreCase("azul") || resposta.equalsIgnoreCase("amarelo")
                                || resposta.equalsIgnoreCase("vermelho") || resposta.equalsIgnoreCase("verde")) {
                            respostaValida = true;
                        }
                        if (!respostaValida) {
                            //Libera a Thread de leitura do Usuario
                            synchronized (perguntaPlayer){
                                perguntaPlayer.notify();
                            }
                            //Aguarda a Thread de leitura do Usuario
                            synchronized (this) {
                                try {
                                    this.wait();
                                } catch (InterruptedException e) {
                                    System.out.println("\nExecption wait! ");
                                }
                            }
                            resposta = perguntaPlayer.mensagem;
                            System.out.print("Resposta Inválida! Tente novamente: ");
                        }
                    } while (!respostaValida);
                    return "6\t" + (jogada[0] - 1) + "\t" + resposta;

                }
                if (carta.getCor().equals(game.getCartaNaMesa().getCor())) {
                    game.getClient().getPlayer().getDeck().remove(jogada[0] - 1);
                    return "6\t" + (jogada[0] - 1);
                }
                if (((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.INVERTE_ORDEM) || ((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.BLOQUEIO)  || ((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.MAIS_2)) {
                    if(game.getCartaNaMesa() instanceof CartaEspecial)
                        if(((CartaEspecial) game.getCartaNaMesa()).getTipoEspecial().equals(((CartaEspecial) carta).getTipoEspecial())){
                            game.getClient().getPlayer().getDeck().remove(jogada[0] - 1);
                            return "6\t" + (jogada[0] - 1);
                        }
                }
            } else {
                if (carta.getCor().equals(game.getCartaNaMesa().getCor())) {
                    game.getClient().getPlayer().getDeck().remove(jogada[0] - 1);
                    return "5\t" + (jogada[0] - 1);
                }
                if (game.getCartaNaMesa() instanceof CartaNormal) {
                    if (((CartaNormal) game.getCartaNaMesa()).getNumero() == ((CartaNormal) carta).getNumero()) {
                        game.getClient().getPlayer().getDeck().remove(jogada[0] - 1);
                        return "5\t" + (jogada[0] - 1);
                    }
                }
            }


        }
        return "err01jogada-invalida01\t";


    }
}









