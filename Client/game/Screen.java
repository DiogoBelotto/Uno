package game;

import baralho.Carta;
import baralho.CartaEspecial;
import baralho.CartaNormal;
import baralho.TipoEspecial;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class Screen {
    private final Game game;
    public static int tipoDePrint;
    private final Scanner scanner;
    Semaphore semaphore;

    public Screen() throws IOException {
        semaphore = new Semaphore(1);
        tipoDePrint = 0;
        scanner = new Scanner(System.in);
        game = new Game();
    }

    public void startScreen() {
        // Inicia a logica do Game em outra Thread para separar essa apenas para
        // impressão
        Thread theared = new Thread(game);
        theared.start();

        // loop que printa a tela do jogo
        do {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                System.out.println("Semaphore Exception");
            }
            boolean var = (tipoDePrint != 0);
            semaphore.release();

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
                        Thread.interrupted();
                        break;
                    case 3:

                }
            }
        } while (true);
    }

    public void preGame() {

        // Pede o nome do usuario para criar o seu Player e envia o nome por socket
        System.out.print("Digite seu username: ");
        game.getClient().enviaMensagem(scanner.nextLine(), 1);

        String resposta = null;
        // Loop para aguardar o jogo iniciar
        while (true) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (Game.podeComecar)
                break;
            semaphore.release();
            if (!game.getClient().getPlayer().isPronto()) {
                System.out.print("Aguardando players ficarem prontos! Se você está pronto digite 'S' ");
                resposta = scanner.nextLine();
            }

            assert resposta != null;
            if (resposta.equalsIgnoreCase("S")) {
                game.getClient().getPlayer().setPronto(true);
                game.getClient().enviaMensagem(null, 3);
                resposta = "";
            }
        }
        System.out.print(Game.podeComecar);

        // Quando podeComecar entra nesse loop
        while (!game.getOpontentes().isEmpty()) {
            // Caso os players tenham desconectado, encerra o Jogo
            // Layout da impressão deve mudar de acordo com a quantidade de players
            switch (game.getNumPlayers()) {
                case 2:
                    System.out.print("[IN]: Invertede Ordem [PJ]: Pula Jogador [MD]: Muda Cor");
                    System.out.print("\r------" + game.getOpontentes().getFirst().getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas");
                    System.out.print("\n\n---------" + game.getCartaNaMesa());
                    System.out.print("\n\n------" + game.getClient().getPlayer().getDeck());
                    isSuaRodada();
                    break;

                case 3:
                    System.out.print("\033[H\033[2J" + "\r------" + "\u001B[32m" + game.getOpontentes().getFirst().getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas" + "\u001B[0m");
                    System.out.print("\n------" + game.getOpontentes().get(1).getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas");
                    System.out.print("\n\n---------" + game.getCartaNaMesa());
                    System.out.print("\n\n------" + game.getClient().getPlayer().getDeck());
                    isSuaRodada();
                    break;

                case 4:
                    System.out.print("\033[H\033[2J" + "\r------" + game.getOpontentes().getFirst().getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas");
                    System.out.print("\n------" + game.getOpontentes().get(1).getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas");
                    System.out.print("\n------" + game.getOpontentes().get(2).getNome() + " ["
                            + game.getOpontentes().getFirst().getQuantCartas() + "] cartas");
                    System.out.print("\n\n---------" + game.getCartaNaMesa());
                    System.out.print("\n\n------" + game.getClient().getPlayer().getDeck());
                    isSuaRodada();
                    break;

            }

            while (game.getClient().getMensagensRecebidas().isEmpty()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println("sleep exception");
                }
            }

        }
        System.out.println("=======Jogo terminou!=======");
        scanner.close();
    }

    private void isSuaRodada() {
        if (game.isSuaRodada()) {
            boolean isJogadaValida = true;
            do {

                System.out.print("\u001B[32m" + "\n--Seu Turno--" + "\u001B[0m");
                System.out.print("\u001B[32m" + "\n--Indique a posição da carta para jogar (de 1 até " + game.getClient().getPlayer().getDeck().size() + "):\u001B[0m");
                System.out.print("\u001B[32m" + "\n--Ou insira -1 para pescar carta--" + "\u001B[0m");
                if (game.getClient().getPlayer().getDeck().size() == 1)
                    System.out.print("\u001B[34m" + "\n--Insira -2 para GRITAR UNO--" + "\u001B[0m");
                for (int i = 0; i < game.getOpontentes().size(); i++) {
                    if (!game.getOpontentes().get(i).isGritouUno() && game.getOpontentes().get(i).getQuantCartas() == 1)
                        System.out.print("\u001B[34m" + "\n--Insira -3 para GRITAR UNO PARA " + game.getOpontentes().get(i).getNome() + " --\u001B[0m");
                }

                String jogada = scanner.nextLine();
                if (!isJogadaValida)
                    System.out.println("Jogada inválida! ");
                String jogadaTraduzidaParaEnvio = verificaJogada(Integer.parseInt(jogada));
                isJogadaValida = !jogadaTraduzidaParaEnvio.equals("err01jogada-invalida01");
                if (isJogadaValida) {
                    game.getClient().enviaMensagem(jogadaTraduzidaParaEnvio, 5);
                }
            } while (!isJogadaValida);
        }
    }

    public String verificaJogada(int jogada) {

        //pescar carta
        if (jogada - 1 == -2) {
            return "4\t";
        }

        //gritar uno
        if (jogada - 1 == -3) {
            if (game.getClient().getPlayer().getDeck().size() == 1)
                return "7\t";
            return "err01jogada-invalida01\t";
        }

        //gritar uno para outro jogador
        if (jogada - 1 == -4) {
            for (int i = 0; i < game.getOpontentes().size(); i++) {
                if (!game.getOpontentes().get(i).isGritouUno() && game.getOpontentes().get(i).getQuantCartas() == 1)
                    return "8\t";
            }
            return "err01jogada-invalida01\t";
        }
        //jogar cartas
        if (jogada - 1 > 0 && jogada - 1 < (game.getClient().getPlayer().getDeck().size())) {
            Carta carta = game.getClient().getPlayer().getDeck().get(jogada - 1);
            if (carta instanceof CartaEspecial) {
                //Caso for um +4 ou Muda Cor
                if (((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.MAIS_4) ||
                        ((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.MUDA_COR)) {
                    game.getClient().getPlayer().getDeck().remove(jogada - 1);

                    //Pede a cor desejada
                    boolean respostaValida = false;
                    System.out.println("Qual a cor desejada? (azul, amarelo, vermelho, verde)");
                    String resposta = scanner.nextLine();
                    do {
                        if (resposta.equalsIgnoreCase("azul") || resposta.equalsIgnoreCase("amarelo")
                                || resposta.equalsIgnoreCase("vermelho") || resposta.equalsIgnoreCase("verde")) {
                            respostaValida = true;
                        }
                        if (!respostaValida) {
                            resposta = scanner.nextLine();
                            System.out.println("Resposta Inválida!");
                        }
                    } while (!respostaValida);
                    return "6\t" + (jogada - 1) + "\t" + resposta;

                }
                if (carta.getCor().equals(game.getCartaNaMesa().getCor())) {
                    game.getClient().getPlayer().getDeck().remove(jogada - 1);
                    return "6\t" + (jogada - 1);
                }
                if (((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.INVERTE_ORDEM) || ((CartaEspecial) carta).getTipoEspecial().equals(TipoEspecial.BLOQUEIO)){
                    game.getClient().getPlayer().getDeck().remove(jogada - 1);
                    return "6\t" + (jogada - 1);
                }
            } else {
                if (carta.getCor().equals(game.getCartaNaMesa().getCor())) {
                    game.getClient().getPlayer().getDeck().remove(jogada - 1);
                    return "5\t" + (jogada - 1);
                }
                if (game.getCartaNaMesa() instanceof CartaNormal) {
                    if (((CartaNormal) game.getCartaNaMesa()).getNumero() == ((CartaNormal) carta).getNumero()){
                        game.getClient().getPlayer().getDeck().remove(jogada - 1);
                        return "5\t" + (jogada - 1);
                    }
                }
            }


        }
        return "err01jogada-invalida01\t";

    }
}









