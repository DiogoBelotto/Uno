package main;

import java.io.IOException;
import java.net.ServerSocket;

import gameServer.Server;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server(new ServerSocket(6789));
        server.startServer();

    }

}
