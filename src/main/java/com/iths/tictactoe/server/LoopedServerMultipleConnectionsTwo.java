package com.iths.tictactoe.server;

import com.iths.tictactoe.Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LoopedServerMultipleConnectionsTwo {
    private ServerSocket serverSocket;

    Model model = new Model();

    private static int playerCount = 0;
    private static int playerTurn = 0;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true)
            new MultiClientHandler(serverSocket.accept()).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private static class MultiClientHandler extends Thread {

        private static List<MultiClientHandler> connectedPlayers = new ArrayList<>();

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public MultiClientHandler(Socket socket) {
            this.clientSocket = socket;
            synchronized (connectedPlayers) {
                connectedPlayers.add(this);
            }
            System.out.println("player" + (++playerCount) + " Connected");
        }

        public void run() {

            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String inputLine;
            while (true) {
                System.out.println(connectedPlayers);
                try {
                    if ((inputLine = in.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (".".equals(inputLine)) {
                    out.println("bye");
                    break;
                }
                if(playerTurn == 0)
                    connectedPlayers.get(1).out.println(inputLine);
                else
                    connectedPlayers.get(0).out.println(inputLine);
                swapPlayerTurn();
            }

            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            out.close();
            try {
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void sendMessageToAll(String message) {
            for (MultiClientHandler player : connectedPlayers) {
                player.sendMessage(message);
            }
        }
        private void sendMessage(String message) {
            out.println(message);
        }
    }

    public static synchronized void swapPlayerTurn() {
        if (playerTurn == 0)
            playerTurn = 1;
        else
            playerTurn = 0;
    }
}