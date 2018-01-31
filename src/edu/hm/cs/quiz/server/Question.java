/**
 * Hochschule München, Fakultät 07. 
 * Softwareentwicklung II Praktikum, IF2A, SS2016
 * Lösung der 2. Aufgabe
 * Oracle Java SE 8u77
 * OS Win7 64, RAM 8GB, CPU 4x2.5GHz x64
 */

package edu.hm.cs.quiz.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A question with a Yes/No answer.
 * @author Thomas Pfaffinger, thomas.pfaffinger@hm.edu
 * @version 1.0
 */
public class Question {

    /**
     * An answer of a player to a question.
     * @author Thomas Pfaffinger, thomas.pfaffinger@hm.edu
     * @version 1.0
     */
    private static final class PlayerAnswer implements Comparable<PlayerAnswer> {
        /** The player who answered. */
        private final Player player;
        /** The answer the player gave. */
        private final Answer answer;
        /** Systemtime [ms] when the player answered. */
        private final long timeStamp;

        /**
         * Ctor.
         * @param player The player.
         * @param answer His answer
         */
        private PlayerAnswer(Player player, Answer answer) {
            this.player = player;
            this.answer = answer;
            this.timeStamp = System.currentTimeMillis();
        }

        private Player getPlayer() {
            return player;
        }

        private Answer getAnswer() {
            return answer;
        }

        private long getTimeStamp() {
            return timeStamp;
        }

        @Override
        public int compareTo(PlayerAnswer arg0) {
            if (arg0 == null) {
                throw new NullPointerException();
            }

            return (int) Math.signum(getTimeStamp() - arg0.getTimeStamp());
        }
    }

    /** The number of points a player gets for answering a question correctly. */
    private static final int NUM_POINTS_CORRECT = 1;
    /** The question text. */
    private final String question;
    /** The correct answer for this question. */
    private final Answer correctAnswer;
    /** The answers from players to this question. */
    private final List<PlayerAnswer> answers;

    /**
     * Creates a new Question-object.
     * @param question The text form of this question.
     * @param answer The correct answer to this question.
     */
    public Question(String question, Answer answer) {
        super();
        this.question = question;
        this.correctAnswer = answer;
        answers = new ArrayList<>();
    }

    /**
     * Yields the question.
     * @return String of the question.
     */
    public String getQuestion() {
        return question;
    }
    
    /**
     * Yields the correct answer to this question.
     * @return The correct answer to this question.
     */
    public Answer getCorrectAnswer() {
        return correctAnswer;
    }

    /**
     * Adds an answer to this question.
     * @param player The player who answered.
     * @param answer The players answer.
     */
    public void addPlayerAnswer(Player player, Answer answer) {
        synchronized (answers) {
            answers.add(new PlayerAnswer(player, answer));
        }
    }
    
    /**
     * True if the given player has answered the question.
     * @param player The player in question.
     * @return True if the player answered the question. 
     */
    public boolean hasPlayerAnswer(Player player) {
        synchronized (answers) {
            return answers.stream().map(answer -> answer.getPlayer()).anyMatch(playerWhoAnswered -> playerWhoAnswered.equals(player));
        }
    }

    /**
     * Provides the number of points a player got for his answer on this question.
     * 
     * @param player The player in question.
     * @return Number of points the player gets for his answer.
     */
    public int getScore(Player player) {
        final int result;
        
        synchronized (answers) {
            if (hasPlayerAnswer(player)) {
                final Optional<PlayerAnswer> optAnswer = answers.stream().filter(answer -> answer.getAnswer().equals(correctAnswer))
                        .sorted().findFirst();
                
                if (optAnswer.isPresent()) {
                    result = optAnswer.get().getPlayer().equals(player) ? NUM_POINTS_CORRECT : 0;
                } else {
                    result = 0;
                }
            } else {
                result = 0;
            } 
        }

        return result;
    }
}
