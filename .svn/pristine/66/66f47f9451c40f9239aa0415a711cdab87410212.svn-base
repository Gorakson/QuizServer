/**
 * Hochschule München, Fakultät 07. 
 * Softwareentwicklung II Praktikum, IF2A, SS2016
 * Lösung der 3. Aufgabe
 * Oracle Java SE 8u77
 * OS Win7 64, RAM 8GB, CPU 4x2.5GHz x64
 */

package edu.hm.cs.quiz.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A connection to the client.
 * @author Thomas Pfaffinger, thoams.pfaffinger@hm.edu
 * @version 1.0
 */
public class ClientConnection implements Runnable {
    /** Indicates that a received/transmitted text is a protocol. */
    private static final String PROTOCOL_IDENTIFIER = "Protocol";
    /** Indicates that a received/transmitted text is an answer. */
    private static final String ANSWER_IDENTIFIER = "Answer:";
    /** Indicates that a received/transmitted text is a question. */
    private static final String QUESTION_IDENTIFIER = "Question:";
    /** Indicates that a received/transmitted text should end the game. */
    private static final String GAME_OVER_IDENTIFIER = "GameOver:";
    /** Connection to the client. */
    private final Socket client;
    /** Receives a message from the client. */
    private final BufferedReader reader;
    /** Writes a message to the client. */
    private final PrintWriter writer;
    /** The player. */
    private final Player player;
    /** The current question to be answered by the player. */
    private Question currentQuestion;
    /** Flag: true, if the game is over, false otherwise. */
    private boolean gameOver;
    /** True if there is a new question. */
    private boolean hasQuestion;
    /** Monitor object for synchronization. */
    private final Object monitor;
    
    /**
     * Ctor.
     * @param client The client
     * @param monitor The monitor object
     * @throws IOException 
     */
    public ClientConnection(Socket client, Object monitor) throws IOException {       
        this.client = client;
        this.player = new Player();
        this.monitor = monitor;
        reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        writer = new PrintWriter(client.getOutputStream(), true);
    }
    
    /**
     * Sets a new question to be answered by the players. 
     * @param question The new question
     */
    public synchronized void setQuestion(Question question) {

        currentQuestion = question;
        hasQuestion = true;
        System.out.println("Got Question");
        notifyAll();
    }
    
    /**
     * Yields the player.
     * @return The player. Not null.
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Closes Input-/Outputstream and client-socket.
     */
    private void close() {
        try {
            reader.close();
            writer.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Tells the client that the game is over.
     * @param message A message to be sent to the clients.
     */
    public synchronized void gameOver(String message) {
        writer.println(GAME_OVER_IDENTIFIER + message);
        gameOver = true;
    }

    @Override
    public void run() {
        writer.println(PROTOCOL_IDENTIFIER + "Welcome " + player.getName());

        while (!gameOver) {
            synchronized (this) {
                while (!hasQuestion && !gameOver) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new AssertionError();
                    }
                }
                if (!gameOver) {
                    postQuestion();
                    receiveAnswer();
                } 
            }
        }
        
        close();
    }
    
    /**
     * Prints the score a player gets for answering the current Question.
     */
    public synchronized void printQuestionScore() {
        final String text = PROTOCOL_IDENTIFIER + currentQuestion.getQuestion() + ", " + currentQuestion.getCorrectAnswer() + ", "
                            + currentQuestion.getScore(player);
        writer.println(text);
    }
    
    /**
     * Sents a text containing the question to the client.
     */
    private void postQuestion() {
        hasQuestion = false;
        writer.println(QUESTION_IDENTIFIER + currentQuestion.getQuestion());
    }
    
    /**
     * Blocks until the player answers to the current question.
     */
    private void receiveAnswer() {
        final String answerText;
        
        try {
            answerText = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        
        final Answer answer;
        
        try {
            answer = Answer.valueOf(answerText.replaceFirst(ANSWER_IDENTIFIER, ""));
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("This really shouldn't happen.");
        }
        
        synchronized (monitor) {
            currentQuestion.addPlayerAnswer(player, answer);
            monitor.notifyAll();
        }
    }
}
