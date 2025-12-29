import java.awt.Point;

/**
 * The Racer class represents a participant in the RU Racing simulation.
 * 
 * Each racer has a position on the track, a battery that needs to be charged,
 * and the ability to teleport using the ScarletTeleporterTM.
 * Racers can only perform one action per simulation step - either charge their battery or teleport.
 * Different racers have different charging requirements based on their algorithmic strategy.
 * 
 * A racer completes the race when they return to the starting position after
 * traveling the full distance of the track.
 *
 * @author Elian Deogracia-Brito
 */
public class Racer {
    private String name;
    private String symbol;
    private Point position;
    private int distance;
    private int battery;
    private int chargeTime;
    private long actions; // Renamed from turn
    private Track track;
    private RacerHistory history;
    
    /**
     * Creates a new racer with the specified properties.
     * 
     * @param name The name of the racer (e.g., "ScarletKnight", "StarbucksTruck")
     * @param symbol The visual symbol representing the racer (e.g., "⚔", "🚚")
     * @param track The track on which the racer will compete
     * @param chargeActions The number of actions needed to fully charge the battery
     *                   (0 for racers that don't require charging)
     * @throws IllegalArgumentException if any parameters are invalid
     */
    public Racer(String name, String symbol, Track track, int chargeActions) {
        if (chargeActions < 0) {
            throw new IllegalArgumentException("Charge actions must be greater than or equal to 0.");
        }
        if (name == null || track == null) {
            throw new IllegalArgumentException("Name, startFinish, and track cannot be null.");
        }
        if (track.getLength() == 0) {
            throw new IllegalArgumentException("Track cannot be empty.");
        }
        
        this.name = name;
        this.symbol = symbol;
        this.distance = 0;
        this.track = track;
        this.position = track.getStartFinish();
        this.actions = 0;
        this.battery = chargeActions;
        this.chargeTime = chargeActions;
    }

    /**
     * Uses the ScarletTeleporterTM to move the racer forward by the specified distance.
     * This action consumes one simulation step and requires a fully charged battery.
     * After teleporting, the battery is reset to 0.
     * 
     * @param distance The number of track positions to teleport forward
     * @throws IllegalArgumentException if the battery isn't fully charged, 
     *         if the position is invalid, or if the teleport would exceed the track length
     */
    public void useScarletTeleporterTM(int distance) {
        // Is the battery sufficient?
        if (battery < chargeTime) {
            throw new IllegalArgumentException("The battery is not charged fully!");
        }

        int i = track.getIndex(position);
        if (i == -1) {
            throw new IllegalArgumentException("Current racer position not found in track.");
        }

        // Move the racer forward by the distance
        int newIndex = i + distance;
        this.distance += distance;
        if (this.distance > track.getLength()) {
            throw new IllegalArgumentException("You went past the finish line!");
        }

        position = track.getPoint(newIndex % track.getLength());
        // Reset the battery
        this.battery = 0;

        finishAction();

        if (history != null) {
            history.onRacerUpdate(this);
        }
    }

    /**
     * Charges the racer's battery by one unit.
     * This action consumes one simulation step.
     * 
     * @throws IllegalArgumentException if the battery is already fully charged
     */
    public void chargeBattery() {
        // Is the battery already charged?
        if (battery >= chargeTime) {
            throw new IllegalArgumentException("The battery is already charged!");
        }

        ++this.battery;

        finishAction();

        if (history != null) {
            history.onRacerUpdate(this);
        }
    }

    /**
     * Completes the current action by incrementing the action counter.
     * This is called automatically by action methods.
     */
    public void finishAction() {
        ++this.actions;
    }

    /**
     * Checks if the racer has completed the race.
     * A race is complete when the racer has traveled the full track length
     * and returned to the starting position.
     * 
     * @return true if the race is complete, false otherwise
     */
    public boolean hasFinishedRace() {
        return position.equals(track.getStartFinish()) && distance == track.getLength();
    }
    
    /**
     * Gets the name of the racer.
     * 
     * @return The racer's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the current position of the racer on the track.
     * 
     * @return The racer's position as a Point
     */
    public Point getPosition() {
        return position;
    }
    
    /**
     * Gets the total distance traveled by the racer.
     * 
     * @return The distance traveled
     */
    public int getDistance() {
        return distance;
    }
    
    /**
     * Gets the track on which the racer is competing.
     * 
     * @return The track object
     */
    public Track getTrack() {
        return track;
    }
    
    /**
     * Gets the current battery level of the racer.
     * 
     * @return The battery level
     */
    public int getBattery() {
        return battery;
    }
    
    /**
     * Gets the number of actions the racer has taken.
     * 
     * @return The number of actions
     */
    public long getActionsCount() {
        return actions;
    }

    /**
     * Gets the number of actions needed to fully charge the racer's battery.
     * 
     * @return The charge actions required
     */
    public int getChargeTime() {
        return chargeTime;
    }

    /**
     * Gets the visual symbol representing the racer.
     * 
     * @return The racer's symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the history of the racer's movements and actions.
     * 
     * @return The racer's history
     */
    public RacerHistory getHistory() {
        return history;
    }
    
    /**
     * Sets the history tracker for this racer.
     * The history tracker records the racer's state after each action.
     * 
     * @param history The history object to use
     */
    public void setHistory(RacerHistory history) {
        this.history = history;
    }

    /**
     * Creates a copy of this racer with the same state.
     * 
     * @return A new Racer object that is a copy of this one
     */
    public Racer clone() {
        Racer copy = new Racer(name, symbol, track, chargeTime);
        copy.position = position;
        copy.distance = distance;
        copy.battery = battery;
        copy.actions = actions;
        return copy;
    }
}