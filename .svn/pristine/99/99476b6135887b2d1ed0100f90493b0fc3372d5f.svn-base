/**
 * Hochschule München, Fakultät 07. 
 * Softwareentwicklung II Praktikum, IF2A, SS2016
 * Lösung der 2. Aufgabe
 * Oracle Java SE 8u77
 * OS Win7 64, RAM 8GB, CPU 4x2.5GHz x64
 */

package edu.hm.cs.quiz.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.IntStream;

/**
 * Server that starts a new games for connecting clients.
 * @author Thomas Pfaffinger, thomas.pfaffinger@hm.edu
 * @version 1.0
 */
public class GameServer implements Runnable {
    /** Port for communication. */
    private static final int PORT = 1337;
    /** Number of players for a game. */
    private static final int NUM_PLAYERS = 2;
    /** Clients that are waiting for a new game. */
    private Queue<Socket> waitingClients;
    
    /**
     * Ctor.
     */
    public GameServer() {
        waitingClients = new LinkedList<>();
    }

    @Override
    public void run() {
        try (final ServerSocket socket = new ServerSocket(PORT)) {
            while (true) {
                final Socket clientSocket = socket.accept();
                waitingClients.add(clientSocket);

                while (waitingClients.size() >= NUM_PLAYERS) {
                    final Collection<ClientConnection> clients = new LinkedList<>();
                    final Object monitor = new Object();
                    IntStream.range(0, NUM_PLAYERS).forEach(unused -> {
                        
                        try {
                            final ClientConnection clientConnection;
                            clientConnection = new ClientConnection(waitingClients.poll(), monitor);
                            clients.add(clientConnection);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                        
                    });
                    
                    final Game game = new Game(clients, monitor);
                    new Thread(game).start();
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    
    /**
     * Starts the server.
     * @param args unused
     */
    public static void main(String... args) {
        new GameServer().run();
    }
}
