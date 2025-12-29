import java.util.ArrayList;
// Removed Scanner and InputMismatchException imports

/**
 * RU Racing: The Algorithmic Grand Prix of Rutgers University
 * =========================================================
 * 
 * In the bustling campus of Rutgers University, a legendary competition has
 * emerged from the
 * intersection of computer science and classic racing: The Algorithmic Grand
 * Prix. This race
 * showcases the practical application of algorithmic complexity through a
 * thrilling competition
 * that pushes the boundaries of teleportation technology.
 * 
 * THE LORE:
 * ---------
 * In the year 2062, a historic collaboration between Rutgers' Computer Science and Physics
 * departments led to a revolutionary breakthrough. Professor Sesh Venugopal from CS and 
 * Dr. Angela Merkel from Physics jointly published a paper on "Quantum-Computational Teleportation
 * Matrices" that shattered previous limitations in quantum mechanics.
 * 
 * Their combined expertise produced the ScarletTeleporter™, harnessing quantum entanglement
 * and computational topology to instantly move objects across space. The prototype, built in
 * the underground labs beneath the Physics Building on Busch Campus, caused quite a stir 
 * when a freshman accidentally teleported an entire batch of Friday the 13th dining hall 
 * meatloaf into President Holloway's office.
 * 
 * Despite this mishap, the ScarletTeleporter™ revolutionized transportation at Rutgers, 
 * allowing students to teleport between campuses in the blink of an eye. However, the 
 * Physics department quickly identified a fundamental constraint: the quantum particle 
 * accelerator powering the device had significant energy requirements tied to Heisenberg's
 * Uncertainty Principle. Different implementations had varying power requirements and 
 * capabilities based on their algorithmic approach to quantum field stabilization.
 * 
 * Seeing an educational opportunity, the departments collaborated to create the Algorithmic
 * Grand Prix - a race that would demonstrate not just teleportation technology, but the 
 * real-world impact of algorithmic efficiency on quantum physical systems.
 * 
 * THE RACERS:
 * -----------
 * 1. ScarletKnight (⚔): The traditional mascot of Rutgers, equipped with the
 * most basic
 * teleporter. This racer demonstrates O(N) linear time complexity, steadily
 * advancing one
 * step at a time without needing to charge. While consistent, this approach
 * lacks the
 * advantage of longer jumps.
 * 
 * 2. StarbucksTruck (🚚): Named after the popular coffee truck often found on
 * College Avenue,
 * this racer exemplifies O(N²) quadratic time complexity. The truck carries
 * enormous batteries
 * that take N turns to charge but only teleport one space at a time, making it
 * reliable but
 * inefficient for long races.
 * 
 * 3. LogNExpress (🚎): Named after the LX bus route that connects Livingston
 * and College Avenue,
 * this racer demonstrates O(log N) logarithmic time complexity. It can double
 * its teleportation
 * distance with each jump (1, 2, 4, 8...) without charging, allowing it to
 * cover ground
 * exponentially faster.
 * 
 * 4. NLogNExpress (🚌): Named after the most efficient sorting algorithms, this
 * racer combines
 * logarithmic jumps with linear charging time, demonstrating O(N log N)
 * complexity. It requires
 * N turns to charge between each jump, but its jump distance doubles each time.
 * 
 * THE RACE:
 * ---------
 * The race takes place on tracks that loop around Rutgers campuses. Each racer
 * must complete
 * exactly one lap, starting and finishing at the same point. Racers are ranked
 * based on:
 * 1. Fewest actions taken (fastest time)
 * 2. In case of a tie, greatest distance traveled
 * 
 * Each racer employs a different strategy based on algorithmic complexity
 * theories:
 * - O(N) linear: Move forward consistently one step per action
 * - O(N²) quadratic: Charge for N actions, then move one step
 * - O(log N) logarithmic: Double teleportation distance with each move action
 * - O(N log N): Charge for N actions, then double teleportation distance
 * 
 * Important rule: In each simulation step where a racer acts, it can perform only ONE action - either charge
 * its battery OR teleport. This constraint creates the different time complexity behaviors.
 * 
 * Will the slow-and-steady ScarletKnight prevail? Or will the logarithmic
 * efficiency of the
 * LogNExpress zoom to victory? The outcome depends on the length of the track
 * and the
 * implementation of each racing strategy.
 * 
 * Students must implement these strategies to understand how algorithmic
 * complexity affects
 * real-world performance!
 * 
 * @author Elian Deogracia-Brito
 */
public class RURacing {
    private ArrayList<Racer> racers;
    private Track track;

    /**
     * Constructs a new race simulation with a specified track.
     * This constructor initializes four racers, each employing a different
     * algorithmic strategy:
     * - ScarletKnight: O(N) linear time complexity
     * - StarbucksTruck: O(N²) quadratic time complexity
     * - LogNExpress: O(log N) logarithmic time complexity
     * - NLogNExpress: O(N log N) time complexity
     * 
     * Note that racers with charging requirements (StarbucksTruck and NLogNExpress)
     * are
     * initialized with a charge actions count equal to the track length, representing the
     * O(N)
     * component of their time complexity.
     * 
     * @param track The track on which the race will be simulated
     * @throws IllegalArgumentException if the track is null
     */
    public RURacing(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track cannot be null.");
        }

        // Initialize each racer with the given track
        this.track = track;
        this.racers = new ArrayList<Racer>();
        this.racers.add(new Racer("ScarletKnight", "⚔", track, 0));
        this.racers.add(new Racer("StarbucksTruck", "🚚", track, track.getLength()));
        this.racers.add(new Racer("LogNExpress", "🚎", track, 0));
        this.racers.add(new Racer("NLogNExpress", "🚌", track, track.getLength()));
    }

    // -----------------------------------------------------
    // STUDENT METHODS - IMPLEMENT THESE METHODS
    // -----------------------------------------------------

    /**
     * O(N²) Strategy - Quadratic time complexity implementation.
     * 
     * TODO: STUDENT - Implement this method
     * 
     * This strategy requires the racer to charge its battery for N actions between
     * each teleportation.
     * For a track of length N, the racer should:
     * - Teleport 1 step immediately on its first action (since battery starts full)
     * - Charge for N actions
     * - Teleport 1 step (total actions for this step: N+1)
     * - Charge for N more actions
     * - Teleport 1 step (total actions for this step: N+1)
     * ... and so on, resulting in O(N²) time complexity.
     * 
     * Note: Each time this racer acts, it can only perform ONE action - either charge or
     * teleport.
     * This racer must spend N actions charging before each teleport (after the first).
     *
     * @param racer The racer to which this strategy is applied
     */
    public static void applyStarbucksTruckAction(Racer racer) {
        if (racer.getBattery() == racer.getChargeTime()) {
            racer.useScarletTeleporterTM(1);
        } else {
            racer.chargeBattery();
        }
    }

    /**
     * O(N log N) Strategy - N * logarithmic time complexity implementation.
     * 
     * TODO: STUDENT - Implement this method
     * 
     * This strategy should combine charging time with exponentially increasing
     * teleport distances:
     * - Teleport 1 step immediately on its first action (since battery starts full)
     * - Charge for N actions
     * - Teleport 2 steps (total actions for this step: N+1)
     * - Charge for N more actions
     * - Teleport 4 steps (total actions for this step: N+1)
     * ... and so on, doubling the distance each time a teleport occurs.
     * 
     * Note: Each time this racer acts, it can only perform ONE action - either charge or
     * teleport.
     * This racer must spend N actions charging before each teleport (after the first) with
     * exponentially increasing distances.
     * 
     * @param racer The racer to which this strategy is applied
     */
    public static void applyNLogNExpressAction(Racer racer) {
        if (racer.getBattery() < racer.getChargeTime()) {
            racer.chargeBattery();
        } else {
            int distanceTraveled = racer.getDistance();
            int exponent;
            if (distanceTraveled == 0) {
                exponent = 0;
            } else {
                exponent = (int)(Math.log(distanceTraveled) / Math.log(2)) + 1;
            }
            int jumpDistance = (int)Math.pow(2, exponent);

            int remainingDistance = racer.getTrack().getLength() - racer.getDistance();
            if (jumpDistance > remainingDistance) {
                jumpDistance = remainingDistance;
            }
            racer.useScarletTeleporterTM(jumpDistance);
        }
    }

    /**
     * O(N) Strategy - Linear time complexity implementation.
     * 
     * TODO: STUDENT - Implement this method
     * 
     * This strategy should make the racer take exactly N teleportation actions to
     * complete the track,
     * with no charging time. The racer should move at a constant speed of 1 step
     * per action,
     * resulting in O(N) time complexity.
     * 
     * Note: Each time this racer acts, it can only perform ONE action - teleport.
     * This racer doesn't need to charge, so it teleports every time it acts.
     *
     * @param racer The racer to which this strategy is applied
     */
    public static void applyScarletKnightAction(Racer racer) {
        racer.useScarletTeleporterTM(1);
    }

    /**
     * O(log N) Strategy - Logarithmic time complexity implementation.
     * 
     * TODO: STUDENT - Implement this method
     * 
     * This strategy should use exponentially increasing teleport distances to
     * complete the track,
     * with no charging time between teleports. The racer should move:
     * - 1 step on action 1
     * - 2 steps on action 2
     * - 4 steps on action 3
     * - 8 steps on action 4
     * ... and so on, doubling the distance with each teleport action.
     * 
     * Note: Each time this racer acts, it can only perform ONE action - teleport.
     * This racer doesn't need to charge, so it teleports every time it acts with
     * exponentially increasing distances.
     * 
     * @param racer The racer to which this strategy is applied
     */
    public static void applyLogNExpressAction(Racer racer) {
        int jumpsDone = (int)(Math.log(racer.getDistance() + 1) / Math.log(2));
        int jumpDistance = (int)Math.pow(2, jumpsDone);
    
        int remainingDistance = racer.getTrack().getLength() - racer.getDistance();
        if (jumpDistance > remainingDistance) {
            jumpDistance = remainingDistance;
        }
        racer.useScarletTeleporterTM(jumpDistance);
    }

    /**
     * Applies the appropriate action strategy for a racer based on their name.
     * Made public so it can be called from the UI.
     * 
     * Each racer gets ONE action per simulation step - either charge their battery or
     * teleport.
     * This method selects and applies the correct strategy based on the racer type.
     * 
     * TODO: STUDENT - Implement this method
     * 
     * @param racer the racer to apply the action strategy to
     */
    public void applyActionStrategy(Racer racer) {
        String name = racer.getName();
        if (name.equals("ScarletKnight")) {
            applyScarletKnightAction(racer);
        } else if (name.equals("StarbucksTruck")) {
            applyStarbucksTruckAction(racer);
        } else if (name.equals("NLogNExpress")) {
            applyNLogNExpressAction(racer);
        } else if (name.equals("LogNExpress")) {
            applyLogNExpressAction(racer);
        }
    }

    /**
     * Static method to rank a list of racers using Insertion Sort (stable).
     * Racers are ranked first by who has traveled the FARTHEST distance,
     * and if there's a tie, then by who has used the FEWEST actions.
     * 
     * TODO: STUDENT - Implement this method
     * 
     * @param racers List of racers to rank
     * @return Ranked list of racers (highest distance first, then lowest action count)
     */
    public static ArrayList<Racer> rankRacers(ArrayList<Racer> racers) {
        for (int i = 1; i < racers.size(); i++) {
            Racer current = racers.get(i);
            int j = i - 1;
        
            while (j >= 0 && (
                current.getDistance() > racers.get(j).getDistance()
                || (current.getDistance() == racers.get(j).getDistance()
                    && current.getActionsCount() < racers.get(j).getActionsCount())
            )) {
                racers.set(j + 1, racers.get(j));
                j--;
            }
            racers.set(j + 1, current);
        }
        return racers;
    }

    /**
     * Simulates a race with a specified maximum number of simulation steps (cutoff).
     * In each step, every racer that hasn't finished performs exactly ONE action based on their strategy
     * (either charging or teleporting). The simulation stops when all racers finish or
     * the cutoff number of steps is reached.
     * 
     * TODO: STUDENT - Implement this method
     * 
     * @param cutoff   Maximum number of simulation steps before ending the simulation
     * @return Ranked list of racers at the end of the simulation
     */
    public ArrayList<Racer> simulateRace(long cutoff) {
        long actions = 0;
        boolean allFinished = false;
    
        while (!allFinished && actions < cutoff) {
            allFinished = true;
            for (Racer racer : racers) {
                if (!racer.hasFinishedRace()) {
                    applyActionStrategy(racer);
                    if (!racer.hasFinishedRace()) {
                        allFinished = false;
                    }
                }
            }
            actions++;
        }
    
        return rankRacers(racers);
    }

    // -----------------------------------------------------
    // PRE-WRITTEN METHODS - DO NOT MODIFY
    // -----------------------------------------------------

    /**
     * Ranks racers based on performance (fewest actions, then distance).
     * 
     * @return List of racers sorted by rank
     */
    public ArrayList<Racer> rankRacers() {
        return rankRacers(new ArrayList<>(racers));
    }

    /**
     * Simulates a race without a specified step limit.
     * This method is a convenience wrapper for the full simulateRace method that
     * uses
     * the maximum possible number of steps as the cutoff.
     */
    public ArrayList<Racer> simulateRace() {
        return simulateRace(Long.MAX_VALUE);
    }

    /**
     * Retrieves the list of racers participating in the simulation.
     * This includes all racers regardless of whether they have finished the race.
     * 
     * @return An unmodifiable list of all racers in the simulation
     */
    public ArrayList<Racer> getRacers() {
        return racers;
    }

    /**
     * Retrieves the track being used for this race simulation.
     * 
     * @return The track object on which the race is taking place
     */
    public Track getTrack() {
        return track;
    }

    /**
     * Retrieves the complete history of each racer's progress throughout the race.
     * This method creates a list of RacerHistory objects, each containing snapshots
     * of
     * a racer's state after each action taken by that racer. This is primarily used for
     * visualization
     * and playback of the race history.
     * 
     * Note: A racer must have a history tracker set via setHistory() before the
     * race
     * for this method to return meaningful data.
     * 
     * @return A list of RacerHistory objects, one for each racer in the simulation
     */
    public ArrayList<RacerHistory> getRacersHistories() {
        ArrayList<RacerHistory> histories = new ArrayList<>();
        for (Racer racer : racers) {
            histories.add(racer.getHistory());
        }
        return histories;
    }
}
