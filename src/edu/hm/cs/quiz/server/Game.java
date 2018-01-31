/**
 * Hochschule München, Fakultät 07. 
 * Softwareentwicklung II Praktikum, IF2A, SS2016
 * Lösung der 2. Aufgabe
 * Oracle Java SE 8u77
 * OS Win7 64, RAM 8GB, CPU 4x2.5GHz x64
 */

package edu.hm.cs.quiz.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Contains all the data for a quiz.
 * @author Thomas Pfaffinger, thomas.pfaffinger@hm.edu
 * @version 1.0
 */
public class Game implements Runnable {

    /** Path to the question database. */
    private static final String QUESTIONS_CSV_PATH = "/src/edu/hm/cs/quiz/server/data/Questions.csv";
    /** Number of questions each player has to answer before the game is over. */
    private static final int NUM_QUESTIONS = 10;
    /** All questions that are answered by the players throughout the game. */
    private final List<Question> questions;
    /** The clients. */
    private final Collection<ClientConnection> clients;
    /** Monitorobject for synchronization. */
    private final Object monitor;
    /** Index of the current Question. */
    private int currentQuestionIndex;
    /** Flag: true if the game is over, false otherwise. */
    private boolean isGameOver;

    /**
     * Ctor.
     * @param clients The clients.
     * @param monitor Monitor for synchronization. 
     */
    public Game(Collection<ClientConnection> clients, Object monitor) {
        if (clients == null) {
            throw new NullPointerException();
        }

        this.questions = readQuestionCsv();
        
        if (questions.isEmpty()) {
            throw new RuntimeException();
        } 
        
        this.clients = clients;
        currentQuestionIndex = 0;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        System.out.println("Game running");
        clients.forEach( client -> new Thread(client).start() );
        clients.forEach( client -> client.setQuestion(getQuestion()) );
        
        while (!isGameOver) {
            synchronized (monitor) {
                while (!clients.stream().map(client -> client.getPlayer()).allMatch(player -> getQuestion().hasPlayerAnswer(player))) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        throw new AssertionError();
                    }
                }
            }
            
            if (!hasNextQuestion()) {
                final Map<Player, Integer> scores = getScores();
                String gameOverMessage = "";
                
                for (ClientConnection client : clients) {
                    gameOverMessage += client.getPlayer().getName() + "; " + scores.get(client.getPlayer()) + "; ";
                }
                
                for (ClientConnection client : clients) {
                    client.printQuestionScore();
                    client.gameOver(gameOverMessage);
                }
            } else {
                nextQuestion();
                clients.forEach( client -> {
                    client.printQuestionScore();
                    client.setQuestion(getQuestion());
                });
            }
        }
    }

    /**
     * Returns the scores for each player.
     * @return Map containing all scores by player
     */
    private Map<Player, Integer> getScores() {
        final Map<Player, Integer> scores = new HashMap<>();

        questions.stream().forEach(question -> {
            clients.stream().forEach(client -> {
                final Player player = client.getPlayer();
                if (scores.containsKey(player)) {
                    scores.put(player, scores.get(player) + question.getScore(player));
                } else {
                    scores.put(player, question.getScore(player));
                }
            });
        });

        return scores;
    }
    
    public synchronized Question getQuestion() {
        return questions.get(currentQuestionIndex);
    }

    /**
     * True if there is a next question.
     * @return True if there is a next question, false otherwise.
     */
    public synchronized boolean hasNextQuestion() {
        return currentQuestionIndex + 1 < questions.size();
    }

    /**
     * Sets the current question to the next question.
     * @return Next question.
     */
    public synchronized Question nextQuestion() {
        final Question result;

        if (hasNextQuestion()) {
            result = questions.get(++currentQuestionIndex);
        } else {
            throw new IllegalStateException();
        }

        return result;
    }

    /**
     * Reads the question database and constructs a list of questions that are to be answered by the players.
     * @return Questions that are to be answered by the players throughout the game
     */
    private List<Question> readQuestionCsv() {
        final List<Question> result = new ArrayList<Question>();
        final List<Question> allQuestions = new ArrayList<Question>();
        final String csvSeperator = ",";
        
        try (final BufferedReader reader = new BufferedReader(new FileReader(Paths.get("").toAbsolutePath() + QUESTIONS_CSV_PATH))) {
            String line = reader.readLine();
            while (line != null) {
                final String[] lineSplit = line.split(csvSeperator);

                if (lineSplit.length != 2) {
                    throw new RuntimeException();
                } else {
                    final String question = lineSplit[0];
                    final Answer answer = Answer.valueOf(lineSplit[1]);
                    allQuestions.add(new Question(question, answer));
                }

                line = reader.readLine();
            }
            
            int questionCounter = 0;
            while (questionCounter < NUM_QUESTIONS && allQuestions.size() > 0) {
                final Random rng = new Random();
                
                result.add(allQuestions.remove(rng.nextInt(allQuestions.size())));
                questionCounter++;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
        }


        return result;
    }
}
