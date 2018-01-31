package edu.hm.cs.quiz.server;

/**
 * A player of a quiz game.
 * @author Thomas Pfaffinger, thomas.pfaffinger@hm.edu
 * @version 1.0
 */
public class Player {
    /** The id that will be given to the next player. */
    private static int nextId = 0;
    /** The id of this player. */
    private final int id;
    
    /**
     * Ctor.
     */
    public Player() {
        id = nextId++;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return "Player " + (id + 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Player other = (Player) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
