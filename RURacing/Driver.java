import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.event.ChangeListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
// Added imports for test harness
import javax.swing.text.DefaultCaret;
import java.util.function.Consumer;

/*
 * @author Elian Deogracia-Brito
 */
public class Driver {
    // Constants
    private static final String TRACK_DIRECTORY = "./";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final long SIMULATION_TIMEOUT_SECONDS = 5;
    private static final int MAX_SLIDER_LABELS = 10;
    private static final String[] TEST_OPTIONS = {
            "1. applyScarletKnightAction (O(N))",
            "2. applyStarbucksTruckAction (O(N^2))",
            "3. applyLogNExpressAction (O(log N))",
            "4. applyNLogNExpressAction (O(N log N))",
            "5. applyActionStrategy (Dispatcher)",
            "6. rankRacers (Static Sorting)"
    };
    private static final String[] RACER_TYPES_FOR_DISPATCHER = {
            "ScarletKnight", "StarbucksTruck", "LogNExpress", "NLogNExpress"
    };

    // UI Components
    private static JFrame frame;
    private static JTabbedPane tabbedPane; // Added

    // Simulation Tab Components
    private static TrackPanel trackPanel;
    private static JComboBox<String> simTrackSelector; // Renamed from trackSelector
    private static JSlider historySlider;
    private static JLabel actionLabel;
    private static JPanel raceInfoPanel;
    private static JButton startButton;
    private static JButton resetButton;

    // Test Harness Tab Components
    private static JPanel testHarnessPanel;
    private static JComboBox<String> testTrackSelector;
    private static JComboBox<String> testMethodSelector;
    private static JComboBox<String> testDispatcherRacerSelector; // For option 5
    private static JSpinner testActionSpinner;
    private static JButton runTestButton;
    private static JTextArea testOutputArea;

    // State
    private static ChangeListener currentSliderListener = null;
    private static List<RacerHistory> currentHistories = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Driver::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = createMainFrame();
        List<String> trackFiles = getTrackFiles(TRACK_DIRECTORY);

        if (trackFiles.isEmpty()) {
            showErrorDialog("No track files found in " + TRACK_DIRECTORY);
            return;
        }

        // --- Create Simulation Tab Components ---
        simTrackSelector = createTrackSelector(trackFiles); // Use simTrackSelector
        Track initialTrack = new Track(TRACK_DIRECTORY + trackFiles.get(0));
        trackPanel = createTrackPanel(initialTrack);
        raceInfoPanel = createRaceInfoPanel();
        historySlider = createHistorySlider();
        actionLabel = createActionLabel();
        startButton = createStartButton();
        resetButton = createResetButton();
        resetButton.setEnabled(false);

        setupTrackSelectionListener(simTrackSelector); // Use simTrackSelector

        JPanel topSimPanel = createTopPanel(simTrackSelector); // Use simTrackSelector
        JPanel controlSimPanel = createControlPanel(startButton, resetButton, actionLabel, historySlider, raceInfoPanel);
        JPanel simulationPanel = new JPanel(new BorderLayout()); // Container for sim tab
        simulationPanel.add(trackPanel, BorderLayout.CENTER);
        simulationPanel.add(topSimPanel, BorderLayout.NORTH);
        simulationPanel.add(controlSimPanel, BorderLayout.SOUTH);

        // --- Create Test Harness Tab Components ---
        testHarnessPanel = createTestHarnessPanel(trackFiles);

        // --- Create Tabbed Pane ---
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Race Simulation", null, simulationPanel, "Visualize the full race simulation");
        tabbedPane.addTab("Test Harness", null, testHarnessPanel, "Test individual strategies and methods");

        frame.add(tabbedPane, BorderLayout.CENTER); // Add tabbed pane to frame

        showFrame();
    }

    // --- GUI Creation Helpers ---

    private static JFrame createMainFrame() {
        JFrame mainFrame = new JFrame("RU Racing Track");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        return mainFrame;
    }

    private static JComboBox<String> createTrackSelector(List<String> trackFiles) {
        return new JComboBox<>(trackFiles.toArray(new String[0]));
    }

    private static TrackPanel createTrackPanel(Track track) {
        return new TrackPanel(track);
    }

    private static JPanel createRaceInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static JSlider createHistorySlider() {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 0, 0);
        slider.setEnabled(false);
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        return slider;
    }

    private static JLabel createActionLabel() {
        return new JLabel("Action: 0");
    }

    private static JButton createStartButton() {
        JButton button = new JButton("Start Simulation");
        button.addActionListener(e -> handleStartSimulation());
        return button;
    }

    private static JButton createResetButton() {
        JButton button = new JButton("Reset Track");
        button.addActionListener(e -> handleResetSimulation());
        return button;
    }

    private static JPanel createTopPanel(JComboBox<String> selector) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Select Track: "));
        panel.add(selector);
        return panel;
    }

    private static JPanel createControlPanel(JButton startBtn, JButton resetBtn, JLabel actionLbl, JSlider slider, JPanel infoPanel) {
        JPanel controlPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(startBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(actionLbl);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(slider, BorderLayout.CENTER);

        JLabel sliderInstructions = new JLabel("Use ← → arrow keys to navigate history");
        sliderInstructions.setForeground(Color.DARK_GRAY);
        sliderInstructions.setHorizontalAlignment(JLabel.CENTER);
        sliderInstructions.setFont(new Font("SansSerif", Font.ITALIC, 11));
        sliderPanel.add(sliderInstructions, BorderLayout.SOUTH);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(sliderPanel, BorderLayout.CENTER);
        controlPanel.add(infoPanel, BorderLayout.SOUTH);

        return controlPanel;
    }

    private static void showFrame() {
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }

    /** Creates the panel for the Test Harness tab. */
    private static JPanel createTestHarnessPanel(List<String> trackFiles) {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Top Controls ---
        JPanel topControls = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Track Selector
        gbc.gridx = 0; gbc.gridy = 0;
        topControls.add(new JLabel("Select Track:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        testTrackSelector = createTrackSelector(trackFiles);
        topControls.add(testTrackSelector, gbc);

        // Method/Strategy Selector
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        topControls.add(new JLabel("Test Target:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        testMethodSelector = new JComboBox<>(TEST_OPTIONS);
        topControls.add(testMethodSelector, gbc);

        // Dispatcher Racer Selector (initially hidden)
        JLabel dispatcherLabel = new JLabel("Racer Type (for Dispatcher):");
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        topControls.add(dispatcherLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        testDispatcherRacerSelector = new JComboBox<>(RACER_TYPES_FOR_DISPATCHER);
        topControls.add(testDispatcherRacerSelector, gbc);
        dispatcherLabel.setVisible(false); // Hide initially
        testDispatcherRacerSelector.setVisible(false); // Hide initially

        // Action Spinner
        JLabel actionsLabel = new JLabel("Actions to Simulate:");
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        topControls.add(actionsLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        testActionSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 10000, 1)); // Default 10, min 0
        topControls.add(testActionSpinner, gbc);

        // Run Button
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        runTestButton = new JButton("Run Test");
        topControls.add(runTestButton, gbc);

        // Add listener to show/hide dispatcher selector and action spinner
        testMethodSelector.addActionListener(e -> {
            int selectedIndex = testMethodSelector.getSelectedIndex();
            boolean showDispatcher = selectedIndex == 4; // Index for "applyActionStrategy"
            boolean showActions = selectedIndex != 5; // Hide for "rankRacers"
            dispatcherLabel.setVisible(showDispatcher);
            testDispatcherRacerSelector.setVisible(showDispatcher);
            actionsLabel.setVisible(showActions);
            testActionSpinner.setVisible(showActions);
        });
        // Trigger listener initially
        testMethodSelector.setSelectedIndex(0);
        testMethodSelector.getActionListeners()[0].actionPerformed(null);

        // --- Output Area ---
        testOutputArea = new JTextArea(15, 60);
        testOutputArea.setEditable(false);
        testOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        // Make scroll pane auto-scroll to bottom
        DefaultCaret caret = (DefaultCaret) testOutputArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(testOutputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // --- Add components to main panel ---
        mainPanel.add(topControls, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Add Action Listener for Run Button ---
        runTestButton.addActionListener(e -> handleRunTest());

        return mainPanel;
    }

    // --- Event Listener Setup ---

    private static void setupTrackSelectionListener(JComboBox<String> selector) { // Parameterized selector
        selector.addActionListener(e -> {
            // Only reset simulation tab if the event came from simTrackSelector
            if (e.getSource() == simTrackSelector) {
                String selectedTrackFile = (String) simTrackSelector.getSelectedItem();
                if (selectedTrackFile != null) {
                    Track newTrack = new Track(TRACK_DIRECTORY + selectedTrackFile);
                    resetUIState(newTrack); // Resets only the simulation tab UI
                }
            }
            // No action needed here if the event came from testTrackSelector
        });
    }

    private static void setupHistorySliderListener() {
        // Remove any existing listener
        if (currentSliderListener != null) {
            historySlider.removeChangeListener(currentSliderListener);
        }

        // Add new listener
        currentSliderListener = ce -> {
            int historyStep = historySlider.getValue();
            updateRaceDisplay(historyStep);
            actionLabel.setText("Action: " + historyStep);
        };
        historySlider.addChangeListener(currentSliderListener);

        SwingUtilities.invokeLater(() -> historySlider.requestFocusInWindow());
    }

    // --- Simulation Logic ---

    private static void handleStartSimulation() {
        RURacing simulation = trackPanel.getSimulation();
        setupHistoryTracking(simulation);

        // Disable start button and enable reset button during/after simulation
        startButton.setEnabled(false);
        resetButton.setEnabled(true);

        try {
            runSimulationWithTimeout(simulation);
            currentHistories = simulation.getRacersHistories(); // Store histories
            updateSliderAfterSimulation(currentHistories);
            updateRaceDisplay(0); // Display initial state
            setupHistorySliderListener();
        } catch (Throwable ex) {
            handleSimulationError(ex);
        }
    }

    private static void handleResetSimulation() {
        // Reset the current track *for the simulation tab*
        String selectedTrackFile = (String) simTrackSelector.getSelectedItem(); // Use sim selector
        if (selectedTrackFile != null) {
            Track newTrack = new Track(TRACK_DIRECTORY + selectedTrackFile);
            resetUIState(newTrack); // Resets only the simulation tab UI
        }
    }

    private static void setupHistoryTracking(RURacing simulation) {
        for (Racer racer : simulation.getRacers()) {
            RacerHistory history = new RacerHistory();
            racer.setHistory(history);
            history.onRacerUpdate(racer); // Record initial state
        }
    }

    private static void runSimulationWithTimeout(RURacing simulation) throws Throwable {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<Racer>> future = executor.submit(() -> {
            // Ensure the correct track file path is used
            return simulation.simulateRace();
        });

        try {
            future.get(SIMULATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutEx) {
            future.cancel(true); // Attempt to interrupt the task
            throw new RuntimeException("Simulation timed out after " + SIMULATION_TIMEOUT_SECONDS
                    + " seconds. Possible infinite loop detected.");
        } catch (ExecutionException execEx) {
            throw execEx.getCause(); // Unwrap and rethrow the original exception
        } catch (InterruptedException interEx) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            throw new RuntimeException("Simulation was interrupted.", interEx);
        } finally {
            executor.shutdownNow(); // Ensure the executor is shut down
        }
    }

    private static void updateSliderAfterSimulation(List<RacerHistory> histories) {
        int maxHistorySteps = 0;
        for (RacerHistory history : histories) {
            maxHistorySteps = Math.max(maxHistorySteps, history.size() - 1);
        }

        boolean enabled = maxHistorySteps > 0;

        // Disable label painting first to avoid issues during range changes
        historySlider.setPaintLabels(false);

        // Set range properties
        historySlider.setMinimum(0);
        historySlider.setMaximum(maxHistorySteps);
        historySlider.setValue(0);

        // Set other slider properties
        historySlider.setEnabled(enabled);
        historySlider.setPaintTicks(enabled);

        // Now it's safe to clear the label table
        historySlider.setLabelTable(null);

        if (enabled) {
            int majorTickSpacing = Math.max(1, maxHistorySteps / MAX_SLIDER_LABELS);
            historySlider.setMajorTickSpacing(majorTickSpacing);

            Hashtable<Integer, JLabel> labelTable = createSliderLabels(maxHistorySteps, majorTickSpacing);
            if (!labelTable.isEmpty()) {
                historySlider.setLabelTable(labelTable);
                historySlider.setPaintLabels(true);
            }
        }

        actionLabel.setText("Action: 0");
    }

    private static Hashtable<Integer, JLabel> createSliderLabels(int maxSteps, int spacing) {
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = 0; i <= maxSteps; i += spacing) {
            labelTable.put(i, new JLabel(String.valueOf(i)));
        }
        // Ensure the last tick is labeled if not already covered
        if (maxSteps % spacing != 0 && maxSteps > 0) {
            labelTable.put(maxSteps, new JLabel(String.valueOf(maxSteps)));
        }
        return labelTable;
    }

    // --- Test Harness Logic ---

    private static void handleRunTest() {
        testOutputArea.setText(""); // Clear previous output
        runTestButton.setEnabled(false); // Disable button during test

        // Get selections
        String selectedTrackFile = (String) testTrackSelector.getSelectedItem();
        int selectedMethodIndex = testMethodSelector.getSelectedIndex();
        int actionsToSimulate = (Integer) testActionSpinner.getValue();
        String selectedDispatcherRacer = (String) testDispatcherRacerSelector.getSelectedItem();

        if (selectedTrackFile == null) {
            appendToTestOutput("Error: No track file selected.");
            runTestButton.setEnabled(true);
            return;
        }

        // Load track in a background thread to avoid freezing GUI
        SwingWorker<Track, Void> trackLoader = new SwingWorker<>() {
            @Override
            protected Track doInBackground() throws Exception {
                appendToTestOutput("Loading track: " + selectedTrackFile + "...");
                return new Track(TRACK_DIRECTORY + selectedTrackFile);
            }

            @Override
            protected void done() {
                try {
                    Track track = get();
                    appendToTestOutput("Track loaded successfully (Length: " + track.getLength() + ").\n");
                    // Proceed with the test in another worker thread
                    runSelectedTest(track, selectedMethodIndex, actionsToSimulate, selectedDispatcherRacer);
                } catch (Exception ex) {
                    appendToTestOutput("Error loading track: " + ex.getMessage());
                    ex.printStackTrace(); // Log details
                    runTestButton.setEnabled(true); // Re-enable button on error
                }
            }
        };
        trackLoader.execute();
    }

    private static void runSelectedTest(Track track, int methodIndex, int actions, String dispatcherRacer) {
        // Run the actual test logic in a background thread
        SwingWorker<Void, String> testWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    switch (methodIndex) {
                        case 0: // applyScarletKnightAction
                        case 1: // applyStarbucksTruckAction
                        case 2: // applyLogNExpressAction
                        case 3: // applyNLogNExpressAction
                            testIndividualStrategyGUI(track, methodIndex + 1, actions); // Pass 1-based index
                            break;
                        case 4: // applyActionStrategy (Dispatcher)
                            testApplyActionStrategyGUI(track, dispatcherRacer, actions);
                            break;
                        case 5: // rankRacers
                            testRankRacersGUI(track);
                            break;
                        default:
                            publish("Error: Invalid test selection.");
                    }
                } catch (Exception e) {
                    publish("\n--- ERROR DURING TEST ---");
                    publish("Error: " + e.getMessage());
                    // Publish stack trace elements for debugging
                    for (StackTraceElement ste : e.getStackTrace()) {
                        publish("  at " + ste.toString());
                    }
                    e.printStackTrace(); // Also print to console
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                // Update GUI with output published from doInBackground
                for (String line : chunks) {
                    appendToTestOutput(line);
                }
            }

            @Override
            protected void done() {
                // Re-enable button when test is finished or errored
                appendToTestOutput("\n--- Test Complete ---");
                runTestButton.setEnabled(true);
            }
        };
        testWorker.execute();
    }

    // --- Test Harness Helper Methods (Adapted from RURacing) ---

    /** Appends text to the test output area safely from any thread. */
    private static void appendToTestOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            testOutputArea.append(text + "\n");
        });
    }

    /** Tests one of the individual static strategy action methods via GUI. */
    private static void testIndividualStrategyGUI(Track track, int strategyChoice, int actionsToSimulate) {
        Racer racer = null;
        Consumer<Racer> strategyAction = null;
        String strategyName = "";

        switch (strategyChoice) {
            case 1:
                racer = new Racer("ScarletKnight", "⚔", track, 0);
                strategyAction = RURacing::applyScarletKnightAction;
                strategyName = "applyScarletKnightAction";
                break;
            case 2:
                racer = new Racer("StarbucksTruck", "🚚", track, track.getLength());
                strategyAction = RURacing::applyStarbucksTruckAction;
                strategyName = "applyStarbucksTruckAction";
                break;
            case 3:
                racer = new Racer("LogNExpress", "🚎", track, 0);
                strategyAction = RURacing::applyLogNExpressAction;
                strategyName = "applyLogNExpressAction";
                break;
            case 4:
                racer = new Racer("NLogNExpress", "🚌", track, track.getLength());
                strategyAction = RURacing::applyNLogNExpressAction;
                strategyName = "applyNLogNExpressAction";
                break;
            default:
                appendToTestOutput("Internal error: Invalid strategy choice passed to testIndividualStrategyGUI.");
                return;
        }
        appendToTestOutput("Testing strategy: " + racer.getName() + " (using static " + strategyName + ")");
        simulateAndPrintGUI(racer, strategyAction, actionsToSimulate, track.getLength());
    }

    /** Tests the non-static applyActionStrategy method via GUI. */
    private static void testApplyActionStrategyGUI(Track track, String racerName, int actionsToSimulate) {
        Racer racer = null;
        switch (racerName) {
            case "ScarletKnight": racer = new Racer("ScarletKnight", "⚔", track, 0); break;
            case "StarbucksTruck": racer = new Racer("StarbucksTruck", "🚚", track, track.getLength()); break;
            case "LogNExpress": racer = new Racer("LogNExpress", "🚎", track, 0); break;
            case "NLogNExpress": racer = new Racer("NLogNExpress", "🚌", track, track.getLength()); break;
            default:
                appendToTestOutput("Error: Invalid racer type selected for dispatcher test.");
                return;
        }

        appendToTestOutput("Testing applyActionStrategy with: " + racer.getName());

        RURacing simulationInstance = new RURacing(track); // Need instance
        Consumer<Racer> strategyAction = r -> simulationInstance.applyActionStrategy(r); // Lambda calls instance method

        simulateAndPrintGUI(racer, strategyAction, actionsToSimulate, track.getLength());
    }

    /** Helper method to run simulation loop and print results to GUI. */
    private static void simulateAndPrintGUI(Racer racer, Consumer<Racer> actionToApply, int actionsToSimulate, int trackLength) {
        appendToTestOutput("\n--- Simulating " + actionsToSimulate + " actions ---");
        appendToTestOutput(String.format("Initial State: Pos=%s, Dist=%d/%d, Bat=%d/%d, Actions=%d",
                racer.getPosition(), racer.getDistance(), trackLength,
                racer.getBattery(), racer.getChargeTime(), racer.getActionsCount()));

        for (int i = 0; i < actionsToSimulate; i++) {
            if (racer.hasFinishedRace()) {
                appendToTestOutput("Racer finished the race early!");
                break;
            }
            try {
                actionToApply.accept(racer); // Apply the chosen action method

                appendToTestOutput(String.format("After Action %d: Pos=%s, Dist=%d/%d, Bat=%d/%d, Actions=%d, Finished=%b",
                        racer.getActionsCount(), // Use actual action count from racer
                        racer.getPosition(),
                        racer.getDistance(), trackLength,
                        racer.getBattery(), racer.getChargeTime(),
                        racer.getActionsCount(),
                        racer.hasFinishedRace()));

            } catch (Exception e) {
                appendToTestOutput(String.format("!!! Error during action %d: %s", racer.getActionsCount() + 1, e.getMessage()));
                // Log stack trace for debugging
                for (StackTraceElement ste : e.getStackTrace()) {
                    appendToTestOutput("  at " + ste.toString());
                }
                e.printStackTrace(); // Also print to console
                break; // Stop simulation on error
            }
        }
    }

    /** Tests the static rankRacers method via GUI. */
    private static void testRankRacersGUI(Track track) { // Use the passed-in track
        appendToTestOutput("\n--- Testing rankRacers ---");

        // Create a list of racers with different states for testing ranking
        List<Racer> testRacers = new ArrayList<>();

        // Use the provided track
        Racer racerA = new Racer("RacerA", "A", track, 0); // Less actions, less dist
        Racer racerB = new Racer("RacerB", "B", track, 0); // More actions, more dist
        Racer racerC = new Racer("RacerC", "C", track, 0); // Less actions, more dist (Winner)
        Racer racerD = new Racer("RacerD", "D", track, 0); // More actions, less dist

        // Simulate actions to achieve desired state (adjust multipliers as needed)
        int baseActions = track.getLength() / 5; // ~20% of track length
        // Ensure baseActions is at least 1 to avoid issues on very short tracks
        baseActions = Math.max(1, baseActions);

        for (int i = 0; i < baseActions; i++) racerA.useScarletTeleporterTM(1); // Dist=base, Actions=base
        for (int i = 0; i < baseActions * 2; i++) racerB.useScarletTeleporterTM(1); // Dist=base*2, Actions=base*2
        for (int i = 0; i < baseActions; i++) racerC.useScarletTeleporterTM(2); // Dist=base*2, Actions=base
        for (int i = 0; i < baseActions * 2; i++) racerD.useScarletTeleporterTM(0); // Dist=0, Actions=base*2 (Charge example)

        testRacers.add(racerA);
        testRacers.add(racerB);
        testRacers.add(racerC);
        testRacers.add(racerD);

        appendToTestOutput("Initial (Unranked) Racers:");
        for (Racer r : testRacers) {
            appendToTestOutput(String.format("  - %s: Actions=%d, Distance=%d", r.getName(), r.getActionsCount(), r.getDistance()));
        }

        // Call the method to be tested
        List<Racer> rankedRacers = RURacing.rankRacers(new ArrayList<>(testRacers)); // Use static method, pass a copy

        appendToTestOutput("\nRanked Racers (Highest Distance First, then Lowest Actions):");
        int rank = 1;
        for (Racer r : rankedRacers) {
            appendToTestOutput(String.format("  %d. %s: Actions=%d, Distance=%d", rank++, r.getName(), r.getActionsCount(), r.getDistance()));
        }
    }

    // --- UI Update Logic ---

    private static void resetUIState(Track newTrack) {
        // --- Reset Simulation Tab ---
        trackPanel.setTrack(newTrack);
        RURacing newSimulation = new RURacing(newTrack);
        trackPanel.setSimulation(newSimulation);
        trackPanel.setRacers(new ArrayList<>()); // Clear racers display
        currentHistories.clear(); // Clear stored histories
        trackPanel.resetView(); // Reset zoom and pan

        startButton.setEnabled(true);
        resetButton.setEnabled(false);

        if (currentSliderListener != null) {
            historySlider.removeChangeListener(currentSliderListener);
            currentSliderListener = null;
        }

        historySlider.setModel(new DefaultBoundedRangeModel(0, 0, 0, 0));
        historySlider.setPaintTicks(false);
        historySlider.setPaintLabels(false);
        historySlider.setEnabled(false);
        historySlider.setLabelTable(null);

        actionLabel.setText("Action: 0");

        raceInfoPanel.removeAll();
        raceInfoPanel.revalidate();
        raceInfoPanel.repaint();

        trackPanel.repaint();

        // --- Reset Test Harness Tab (Optional - maybe just clear output?) ---
        // testOutputArea.setText(""); // Optionally clear test output on sim reset
    }

    private static void updateRaceDisplay(int historyStep) {
        if (currentHistories.isEmpty()) {
            return; // Nothing to display
        }

        // Get racers at the specified history step from stored histories
        List<Racer> racersAtStep = getRacersAtAction(currentHistories, historyStep);
        List<Racer> previousRacers = (historyStep > 0) ? getRacersAtAction(currentHistories, historyStep - 1) : new ArrayList<>();

        // Detect turn effects (teleportations and charging)
        List<TurnEffect> turnEffects = detectTurnEffects(racersAtStep, previousRacers);

        // Update track panel
        trackPanel.setRacers(racersAtStep);
        trackPanel.setTurnEffects(turnEffects); // Use the new method
        trackPanel.startAnimation(); // This will trigger repaint

        // Update info panel
        updateInfoPanel(racersAtStep);
    }

    private static List<Racer> getRacersAtAction(List<RacerHistory> histories, int actionIndex) {
        List<Racer> racersAtAction = new ArrayList<>();
        for (RacerHistory history : histories) {
            int historyIndex = Math.min(actionIndex, history.size() - 1);
            if (historyIndex >= 0) {
                racersAtAction.add(history.getActionSnapshot(historyIndex));
            }
        }
        return racersAtAction;
    }

    private static List<TurnEffect> detectTurnEffects(List<Racer> currentRacers, List<Racer> previousRacers) {
        List<TurnEffect> effects = new ArrayList<>();
        Map<String, Racer> previousRacerMap = new HashMap<>();
        Map<String, Point> previousScreenPositions = trackPanel.getRacerScreenPositions(); // Get previous screen positions

        // Build map of previous racers
        for (Racer racer : previousRacers) {
            previousRacerMap.put(racer.getName(), racer);
        }

        // Compare current racers with previous state
        for (Racer currentRacer : currentRacers) {
            Racer previousRacer = previousRacerMap.get(currentRacer.getName());
            Point previousScreenPos = previousScreenPositions.get(currentRacer.getName());

            if (previousRacer != null) {
                // Check for teleportation (position change)
                if (!previousRacer.getPosition().equals(currentRacer.getPosition())) {
                    effects.add(new TeleportEffect(
                            currentRacer.getName(),
                            previousRacer.getPosition(),
                            currentRacer.getPosition(),
                            previousScreenPos // Pass the precise previous screen position
                    ));
                }
                // Check for charging (position same, battery increased)
                else if (currentRacer.getBattery() > previousRacer.getBattery()) {
                    effects.add(new ChargeEffect(
                            currentRacer.getName(),
                            currentRacer.getPosition() // Position where charging occurred
                    ));
                }
            }
        }
        return effects;
    }

    private static void updateInfoPanel(List<Racer> racersAtTurn) {
        raceInfoPanel.removeAll();

        List<Racer> rankedRacers = RURacing.rankRacers(new ArrayList<>(racersAtTurn));
        Map<String, Integer> racerRanks = new HashMap<>();
        for (int i = 0; i < rankedRacers.size(); i++) {
            racerRanks.put(rankedRacers.get(i).getName(), i + 1);
        }

        JLabel rankingTitle = new JLabel("Current Rankings:");
        rankingTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        raceInfoPanel.add(rankingTitle);

        for (Racer racer : rankedRacers) {
            int rank = racerRanks.get(racer.getName());
            String status = racer.hasFinishedRace() ? " [FINISHED]" : "";
            JLabel racerInfo = new JLabel(
                    String.format("#%d - %s (%s)%s: Pos (%d,%d), Dist: %d/%d, Bat: %d/%d, Actions: %d",
                            rank,
                            racer.getName(), racer.getSymbol(), status,
                            racer.getPosition().x, racer.getPosition().y,
                            racer.getDistance(), racer.getTrack().getLength(),
                            racer.getBattery(), racer.getChargeTime(),
                            racer.getActionsCount()));

            // Set color based on rank
            racerInfo.setForeground(getRankColor(rank));
            raceInfoPanel.add(racerInfo);
        }

        raceInfoPanel.revalidate();
        raceInfoPanel.repaint();
    }

    private static Color getRankColor(int rank) {
        switch (rank) {
            case 1: return new Color(218, 165, 32); // Gold
            case 2: return new Color(112, 128, 144); // Silver (SlateGray)
            case 3: return new Color(176, 141, 87); // Bronze
            default: return Color.BLACK;
        }
    }

    // --- Utility and Error Handling ---

    private static List<String> getTrackFiles(String directoryPath) {
        List<String> trackFiles = new ArrayList<>();
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".in"));
            if (files != null) {
                for (File file : files) {
                    trackFiles.add(file.getName());
                }
                // Sort files alphabetically for consistent order
                trackFiles.sort(String.CASE_INSENSITIVE_ORDER);
            }
        }
        return trackFiles;
    }

    private static void handleSimulationError(Throwable ex) {
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "An unknown error occurred.";
        String errorSource = "an error in your implementation";
        String specificMethod = "";

        if (errorMessage.contains("infinite loop")) {
            errorSource = "a possible infinite loop";
        } else if (ex instanceof Exception) {
            StackTraceElement[] stackTrace = ex.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                String methodName = element.getMethodName();
                // Try to identify a strategy method
                if (methodName.contains("apply") && methodName.contains("Turn")) {
                    specificMethod = " in the " + methodName + " method";
                    break;
                }
            }
            if (!specificMethod.isEmpty()) {
                errorSource = "an error" + specificMethod;
            }
        }

        String fullMessage = String.format(
                "An error occurred during simulation:\n%s\n\nThe simulation failed due to %s.\nCheck your implementation.",
                errorMessage, errorSource);

        showErrorDialog(fullMessage);
        ex.printStackTrace(); // Log full trace for debugging
    }

    private static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ========================================================================
    // Inner Class: TrackPanel - For visualizing the track
    // ========================================================================
    static class TrackPanel extends JPanel {
        // Constants
        private static final int DEFAULT_CELL_SIZE = 30;
        private static final int LEFT_PADDING = 200;
        private static final int TOP_PADDING = 50;
        private static final int RIGHT_PADDING = 50;
        private static final int BOTTOM_PADDING = 50;
        private static final int ANIMATION_FRAMES = 20; // Total frames for teleport
        private static final int PATH_FRAMES = 5; // Frames to show just the path
        private static final int ANIMATION_DELAY_MS = 50; // Milliseconds per frame
        private static final double MIN_ZOOM = 0.2;
        private static final double MAX_ZOOM = 8.0;
        private static final double ZOOM_SENSITIVITY = 0.1;

        // State
        private Track track;
        private RURacing simulation;
        private List<Racer> racers = new ArrayList<>();
        private Map<String, Point> racerScreenPositions = new HashMap<>(); // Tracks center point of each racer on screen
        private int baseCellSize = DEFAULT_CELL_SIZE; // Renamed from cellSize
        private int arcSize = baseCellSize / 2;

        // Zoom and Pan State
        private double zoomLevel = 1.0;
        private Point2D.Double viewOffset = new Point2D.Double(0, 0); // Top-left corner in grid units
        private Point mouseDragStartPoint = null; // For panning
        private Point2D.Double viewOffsetDragStart = null; // For panning

        // Animation
        private List<TurnEffect> turnEffects = new ArrayList<>(); // Changed from TeleportationEffect
        private boolean animating = false;
        private int animationFrame = 0;
        private Timer animationTimer;

        public TrackPanel(Track track) {
            this.track = track;
            this.simulation = new RURacing(track);
            setBackground(Color.WHITE);
            setupAnimationTimer();
            addZoomAndPanListeners(); // Add listeners
        }

        private void setupAnimationTimer() {
            animationTimer = new Timer(ANIMATION_DELAY_MS, e -> {
                animationFrame++;
                if (animationFrame >= ANIMATION_FRAMES) {
                    stopAnimation();
                }
                repaint(); // Trigger repaint for each animation frame
            });
        }

        public void startAnimation() {
            if (!turnEffects.isEmpty()) { // Check the generalized list
                animating = true;
                animationFrame = 0;
                animationTimer.restart();
            } else {
                // If no effects, ensure repaint happens once
                repaint();
            }
        }

        private void stopAnimation() {
            animationFrame = 0;
            animating = false;
            turnEffects.clear(); // Clear the generalized list
            if (animationTimer.isRunning()) {
                animationTimer.stop();
            }
            repaint(); // Final repaint in non-animating state
        }

        // --- Zoom and Pan ---
        private void addZoomAndPanListeners() {
            this.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    handleZoom(e);
                }
            });

            MouseAdapter panAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Trigger pan on Middle Mouse Button OR Shift + Left Mouse Button
                    if (SwingUtilities.isMiddleMouseButton(e) || (SwingUtilities.isLeftMouseButton(e))) {
                        mouseDragStartPoint = e.getPoint();
                        viewOffsetDragStart = new Point2D.Double(viewOffset.x, viewOffset.y);
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (mouseDragStartPoint != null && viewOffsetDragStart != null) {
                        handlePan(e); // Call pan logic during drag
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (mouseDragStartPoint != null) {
                        // Reset panning state on release
                        mouseDragStartPoint = null;
                        viewOffsetDragStart = null;
                        setCursor(Cursor.getDefaultCursor());
                        repaint(); // Repaint once after drag ends for clarity
                    }
                }
            };
            this.addMouseListener(panAdapter);
            this.addMouseMotionListener(panAdapter);
        }

        private void handleZoom(MouseWheelEvent e) {
            double zoomFactor = Math.pow(1 + ZOOM_SENSITIVITY, -e.getPreciseWheelRotation());
            double newZoomLevel = zoomLevel * zoomFactor;
            newZoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoomLevel)); // Clamp zoom

            if (Math.abs(newZoomLevel - zoomLevel) < 1e-6) return; // No significant change

            Point mousePoint = e.getPoint();
            Point2D.Double mouseGridPosBefore = screenToGrid(mousePoint);

            zoomLevel = newZoomLevel;
            double effectiveCellSize = getEffectiveCellSize();

            // Adjust offset to keep the point under the mouse stationary
            viewOffset.x = mouseGridPosBefore.x - (mousePoint.x - LEFT_PADDING) / effectiveCellSize;
            viewOffset.y = mouseGridPosBefore.y - (mousePoint.y - TOP_PADDING) / effectiveCellSize;

            clampViewOffset();
            repaint();
        }

        private void handlePan(MouseEvent e) {
            double effectiveCellSize = getEffectiveCellSize();
            if (effectiveCellSize <= 0) return;

            // Calculate mouse movement delta in screen pixels
            double dxScreen = e.getX() - mouseDragStartPoint.x;
            double dyScreen = e.getY() - mouseDragStartPoint.y;

            // Convert screen delta to grid delta
            double dxGrid = dxScreen / effectiveCellSize;
            double dyGrid = dyScreen / effectiveCellSize;

            // Update view offset based on starting offset and grid delta
            // Subtract delta because moving mouse right should move view left (content moves right)
            viewOffset.x = viewOffsetDragStart.x - dxGrid;
            viewOffset.y = viewOffsetDragStart.y - dyGrid;

            clampViewOffset(); // Keep view within bounds
            repaint(); // Redraw with new offset
        }

        private void clampViewOffset() {
            Dimension trackSize = getTrackGridDimensions();
            double effectiveCellSize = getEffectiveCellSize();
            // Calculate the width/height of the view in grid units
            double viewWidthGrid = (getWidth() - LEFT_PADDING - RIGHT_PADDING) / effectiveCellSize;
            double viewHeightGrid = (getHeight() - TOP_PADDING - BOTTOM_PADDING) / effectiveCellSize;

            // Allow panning slightly beyond the edges (e.g., 10% of the view size)
            double bufferX = viewWidthGrid * 0.1;
            double bufferY = viewHeightGrid * 0.1;

            // Clamp the view offset
            // Min offset allows bufferX grid units to the left/top of the track (negative offset)
            // Max offset allows bufferX grid units to the right/bottom of the track
            viewOffset.x = Math.max(-bufferX, Math.min(trackSize.width - viewWidthGrid + bufferX, viewOffset.x));
            viewOffset.y = Math.max(-bufferY, Math.min(trackSize.height - viewHeightGrid + bufferY, viewOffset.y));

            // If the track is smaller than the view (zoomed out), center it
            if (trackSize.width < viewWidthGrid) {
                viewOffset.x = (trackSize.width - viewWidthGrid) / 2.0; // Center horizontally
            }
            if (trackSize.height < viewHeightGrid) {
                viewOffset.y = (trackSize.height - viewHeightGrid) / 2.0; // Center vertically
            }
        }

        public void resetView() {
            zoomLevel = 1.0;
            viewOffset.setLocation(0, 0);
            calculateBaseCellSize(); // Recalculate base size based on new track if needed
            clampViewOffset(); // Center if needed after reset
            repaint();
        }

        private double getEffectiveCellSize() {
            return baseCellSize * zoomLevel;
        }

        // --- Getters and Setters ---
        public void setTrack(Track track) { this.track = track; }
        public Track getTrack() { return track; }
        public void setRacers(List<Racer> racers) { this.racers = racers; }
        public List<Racer> getRacers() { return racers; }
        public RURacing getSimulation() { return simulation; }
        public void setSimulation(RURacing simulation) { this.simulation = simulation; }
        public Map<String, Point> getRacerScreenPositions() { return racerScreenPositions; }
        public void setTurnEffects(List<TurnEffect> effects) { this.turnEffects = effects; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            setupRenderingHints(g2d);
            calculateBaseCellSize(); // Ensure base size is up-to-date

            // Store previous screen positions before recalculating
            Map<String, Point> previousPositionsForAnimation = new HashMap<>(racerScreenPositions);
            racerScreenPositions.clear(); // Clear for recalculation

            // Calculate current screen positions (needed for teleport effects AND drawing racers)
            calculateRacerScreenPositions(); // Uses zoom/offset via gridToScreen

            // Apply clipping to the drawing area
            Shape oldClip = g2d.getClip();
            g2d.clipRect(LEFT_PADDING, TOP_PADDING, getWidth() - LEFT_PADDING - RIGHT_PADDING, getHeight() - TOP_PADDING - BOTTOM_PADDING);

            // Draw components
            drawTrackBase(g2d);
            drawGridOutlines(g2d);
            drawStartFinish(g2d);

            // Draw racers based on the current state FIRST
            drawRacers(g2d);

            // Draw turn effects ON TOP only if animating
            if (animating) {
                drawTurnEffects(g2d, previousPositionsForAnimation);
            }

            // Restore clip and draw legend
            g2d.setClip(oldClip);
            drawLegend(g2d); // Draw legend last so it's on top and not clipped by track area
        }

        private void setupRenderingHints(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }

        private Dimension getTrackGridDimensions() {
            if (track == null || track.getTrack().isEmpty()) {
                return new Dimension(1, 1);
            }
            int maxX = 0;
            int maxY = 0;
            for (Point p : track.getTrack()) {
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }
            return new Dimension(maxX + 1, maxY + 1);
        }

        private void calculateBaseCellSize() {
            // Calculate the cell size needed to fit the entire track without zoom
            Dimension trackSize = getTrackGridDimensions();
            if (trackSize.width <= 0 || trackSize.height <= 0) {
                baseCellSize = DEFAULT_CELL_SIZE;
                arcSize = baseCellSize / 2;
                return;
            }

            int availableWidth = getWidth() - LEFT_PADDING - RIGHT_PADDING;
            int availableHeight = getHeight() - TOP_PADDING - BOTTOM_PADDING;

            baseCellSize = Math.max(1, Math.min(availableWidth / trackSize.width, availableHeight / trackSize.height));
            arcSize = baseCellSize / 2; // Base arc size
        }

        private void drawLegend(Graphics2D g2d) {
            // Semi-transparent background
            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.fillRoundRect(10, 10, 180, 180, 10, 10);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawRoundRect(10, 10, 180, 180, 10, 10);

            // Title
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Track Visualization", 20, 30);

            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            int legendY = 45;
            int itemHeight = 25;
            int symbolSize = 15;
            int textX = 40;

            // Start/Finish
            g2d.setColor(Color.GRAY);
            g2d.fillRoundRect(20, legendY, symbolSize, symbolSize, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawString(" - Start/Finish", textX, legendY + symbolSize - 2);
            legendY += itemHeight;

            // Track
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRoundRect(20, legendY, symbolSize, symbolSize, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawString(" - Track", textX, legendY + symbolSize - 2);
            legendY += itemHeight;

            // Racers
            // Use a temporary list to avoid concurrent modification if racers change
            List<Racer> currentRacers = new ArrayList<>(this.racers);
            for (Racer racer : currentRacers) {
                if (legendY > 180) break; // Don't overflow legend box
                g2d.setColor(getRacerColor(racer.getName()));
                g2d.fillOval(20, legendY, symbolSize, symbolSize);
                g2d.setColor(Color.BLACK);
                g2d.drawString(" - " + racer.getName(), textX, legendY + symbolSize - 2);
                legendY += itemHeight - 5; // Slightly less spacing for racers
            }
        }

        private void drawTrackBase(Graphics2D g2d) {
            if (track == null || track.getTrack().isEmpty()) return;

            double effectiveCellSize = getEffectiveCellSize();

            g2d.setColor(Color.DARK_GRAY);
            // Scale stroke width with zoom, but clamp to avoid extreme thickness/thinness
            float strokeWidth = (float) Math.max(1.0, Math.min(effectiveCellSize, baseCellSize * zoomLevel));
            g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Point[] trackPoints = track.getTrack().toArray(new Point[0]);
            for (int i = 0; i < trackPoints.length; i++) {
                Point current = trackPoints[i];
                Point next = trackPoints[(i + 1) % trackPoints.length]; // Wrap around

                Point p1 = gridToScreen(current); // Uses zoom/offset
                Point p2 = gridToScreen(next);   // Uses zoom/offset

                // Adjust draw position to center the stroke
                double halfStroke = effectiveCellSize / 2.0;
                g2d.drawLine((int) (p1.x + halfStroke), (int) (p1.y + halfStroke),
                             (int) (p2.x + halfStroke), (int) (p2.y + halfStroke));
            }
        }

        private void drawGridOutlines(Graphics2D g2d) {
            if (track == null || track.getTrack().isEmpty()) return;

            double effectiveCellSize = getEffectiveCellSize();
            if (effectiveCellSize < 5) return; // Don't draw grid if cells are too small

            Dimension trackSize = getTrackGridDimensions();
            int maxX = trackSize.width - 1;
            int maxY = trackSize.height - 1;

            g2d.setColor(new Color(200, 200, 200)); // Light gray
            g2d.setStroke(new BasicStroke(1.0f));

            // Determine visible grid range based on viewOffset and panel size
            Point2D.Double topLeftDouble = screenToGrid(new Point(LEFT_PADDING, TOP_PADDING));
            Point2D.Double bottomRightDouble = screenToGrid(new Point(getWidth() - RIGHT_PADDING, getHeight() - BOTTOM_PADDING));

            Point topLeft = new Point((int) Math.floor(topLeftDouble.x), (int) Math.floor(topLeftDouble.y));
            Point bottomRight = new Point((int) Math.ceil(bottomRightDouble.x), (int) Math.ceil(bottomRightDouble.y));

            int startX = Math.max(0, topLeft.x);
            int endX = Math.min(maxX, bottomRight.x);
            int startY = Math.max(0, topLeft.y);
            int endY = Math.min(maxY, bottomRight.y);

            // Draw only visible lines
            for (int y = startY; y <= endY + 1; y++) {
                Point p1 = gridToScreen(new Point(startX, y));
                Point p2 = gridToScreen(new Point(endX + 1, y));
                g2d.drawLine(p1.x, p1.y, p2.x, p1.y);
            }
            for (int x = startX; x <= endX + 1; x++) {
                Point p1 = gridToScreen(new Point(x, startY));
                Point p2 = gridToScreen(new Point(x, endY + 1));
                g2d.drawLine(p1.x, p1.y, p1.x, p2.y);
            }
        }

        private void drawStartFinish(Graphics2D g2d) {
            if (track == null || track.getStartFinish() == null) return;

            double effectiveCellSize = getEffectiveCellSize();
            int effectiveArcSize = (int) Math.max(5, arcSize * zoomLevel); // Ensure minimum arc size

            Point startFinish = track.getStartFinish();
            Point screenPos = gridToScreen(startFinish); // Uses zoom/offset

            g2d.setColor(Color.GRAY);
            g2d.fillRoundRect(screenPos.x, screenPos.y, (int) effectiveCellSize, (int) effectiveCellSize, effectiveArcSize, effectiveArcSize);

            // Draw "S/F" text only if cell is large enough
            if (effectiveCellSize >= 15) {
                g2d.setColor(Color.WHITE);
                int fontSize = Math.max(10, (int) (effectiveCellSize / 3));
                g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "S/F";
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();
                g2d.drawString(text, screenPos.x + (int) (effectiveCellSize - textWidth) / 2,
                               screenPos.y + (int) (effectiveCellSize + textHeight) / 2 - fm.getDescent());
            }
        }

        /** Calculates and stores the screen center positions for all current racers. */
        private void calculateRacerScreenPositions() {
            Map<Point, List<Racer>> racersByGridPos = groupRacersByPosition();
            double effectiveCellSize = getEffectiveCellSize();

            for (Map.Entry<Point, List<Racer>> entry : racersByGridPos.entrySet()) {
                Point gridPos = entry.getKey();
                List<Racer> racersAtPos = entry.getValue();
                Point cellScreenPos = gridToScreen(gridPos); // Top-left of the cell, uses zoom/offset

                int numRacers = racersAtPos.size();
                int cols = (int) Math.ceil(Math.sqrt(numRacers));
                int rows = (int) Math.ceil(numRacers / (double) cols);

                // Scale racer size and padding with zoom
                int baseRacerSize = Math.max(4, Math.min(baseCellSize / Math.max(cols, rows), baseCellSize / 2));
                int effectiveRacerSize = Math.max(4, (int)(baseRacerSize * zoomLevel));
                int padding = Math.max(1, effectiveRacerSize / 4);

                int gridWidth = cols * (effectiveRacerSize + padding) - padding;
                int gridHeight = rows * (effectiveRacerSize + padding) - padding;
                int startX = cellScreenPos.x + (int) (effectiveCellSize - gridWidth) / 2;
                int startY = cellScreenPos.y + (int) (effectiveCellSize - gridHeight) / 2;

                for (int i = 0; i < numRacers; i++) {
                    Racer racer = racersAtPos.get(i);
                    int row = i / cols;
                    int col = i % cols;
                    int racerX = startX + col * (effectiveRacerSize + padding);
                    int racerY = startY + row * (effectiveRacerSize + padding);

                    // Store the CENTER position
                    racerScreenPositions.put(racer.getName(), new Point(racerX + effectiveRacerSize / 2, racerY + effectiveRacerSize / 2));
                }
            }
        }

        private void drawRacers(Graphics2D g2d) {
            Map<Point, List<Racer>> racersByGridPos = groupRacersByPosition();

            for (Map.Entry<Point, List<Racer>> entry : racersByGridPos.entrySet()) {
                List<Racer> racersAtPos = entry.getValue();
                int numRacers = racersAtPos.size();
                int cols = (int) Math.ceil(Math.sqrt(numRacers));
                int rows = (int) Math.ceil(numRacers / (double) cols);

                // Recalculate effective racer size for drawing
                int baseRacerSize = Math.max(4, Math.min(baseCellSize / Math.max(cols, rows), baseCellSize / 2));
                int effectiveRacerSize = Math.max(4, (int)(baseRacerSize * zoomLevel));

                for (Racer racer : racersAtPos) {
                    Point centerPos = racerScreenPositions.get(racer.getName());
                    if (centerPos != null) {
                        // Calculate top-left from center for drawing
                        drawSingleRacer(g2d, racer, centerPos.x - effectiveRacerSize / 2, centerPos.y - effectiveRacerSize / 2, effectiveRacerSize);
                    }
                }
            }
        }

        private void drawSingleRacer(Graphics2D g2d, Racer racer, int x, int y, int size) {
            g2d.setColor(getRacerColor(racer.getName()));
            g2d.fillOval(x, y, size, size);

            // Draw symbol only if size is sufficient
            if (size >= 12) { // Threshold for drawing symbol
                g2d.setColor(Color.WHITE);
                int fontSize = Math.max(8, size / 2);
                g2d.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                FontMetrics fm = g2d.getFontMetrics();
                String symbol = racer.getSymbol();
                int textWidth = fm.stringWidth(symbol);
                int textHeight = fm.getAscent();
                // Center symbol in the oval
                g2d.drawString(symbol,
                               x + (size - textWidth) / 2,
                               y + (size + textHeight) / 2 - fm.getDescent());
            }
        }

        private Map<Point, List<Racer>> groupRacersByPosition() {
            Map<Point, List<Racer>> map = new HashMap<>();
            // Use a temporary list to avoid concurrent modification
            List<Racer> currentRacers = new ArrayList<>(this.racers);
            for (Racer racer : currentRacers) {
                map.computeIfAbsent(racer.getPosition(), k -> new ArrayList<>()).add(racer);
            }
            return map;
        }

        private void drawTurnEffects(Graphics2D g2d, Map<String, Point> previousScreenPositions) {
            // Effects use racerScreenPositions (current) and previousScreenPositions
            // Need to ensure effect drawing scales with zoom
            for (TurnEffect effect : turnEffects) {
                if (effect instanceof TeleportEffect) {
                    // Pass previous *screen* position if available, otherwise calculate from grid
                    Point prevScreen = ((TeleportEffect) effect).previousScreenPos;
                    if (prevScreen == null) {
                        prevScreen = gridToScreenCenter(((TeleportEffect) effect).from);
                    }
                    drawTeleportEffect(g2d, (TeleportEffect) effect, prevScreen);
                } else if (effect instanceof ChargeEffect) {
                    drawChargeEffect(g2d, (ChargeEffect) effect);
                }
            }
        }

        private void drawTeleportEffect(Graphics2D g2d, TeleportEffect effect, Point fromScreenPos) {
            Point toScreenPos = racerScreenPositions.get(effect.racerName); // Use current calculated position

            if (toScreenPos == null) {
                toScreenPos = gridToScreenCenter(effect.to); // Fallback
            }

            double effectiveCellSize = getEffectiveCellSize();

            if (animationFrame < PATH_FRAMES) {
                // Phase 1: Draw path and fading racer at start
                drawTeleportPath(g2d, effect, fromScreenPos, toScreenPos);
                drawFadingRacerAtStart(g2d, effect, fromScreenPos, effectiveCellSize);
            } else {
                // Phase 2: Draw moving particles and flash at end
                drawTeleportMovement(g2d, effect, fromScreenPos, toScreenPos, effectiveCellSize); // Pass size
            }
        }

        private void drawTeleportPath(Graphics2D g2d, TeleportEffect effect, Point from, Point to) {
            Color racerColor = getRacerColor(effect.racerName);
            Color trailColor = new Color(racerColor.getRed(), racerColor.getGreen(), racerColor.getBlue(), 128); // Semi-transparent

            float dashPhase = (float) (animationFrame % 10);
            float strokeWidth = Math.max(1f, (float)(3 * zoomLevel)); // Scale stroke
            float[] dash = { Math.max(2f, 5f * (float)zoomLevel), Math.max(2f, 5f * (float)zoomLevel) }; // Scale dash pattern
            g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, dashPhase));
            g2d.setColor(trailColor);
            g2d.drawLine(from.x, from.y, to.x, to.y);
        }

        private void drawFadingRacerAtStart(Graphics2D g2d, TeleportEffect effect, Point startPos, double effectiveCellSize) {
            int transparency = 255 - (animationFrame * (255 / PATH_FRAMES));
            transparency = Math.max(0, Math.min(255, transparency)); // Clamp

            Color racerColor = getRacerColor(effect.racerName);
            Color fadedColor = new Color(racerColor.getRed(), racerColor.getGreen(), racerColor.getBlue(), transparency);

            g2d.setColor(fadedColor);

            // Calculate size consistent with drawSingleRacer (assuming 1 racer for simplicity here)
            int baseRacerSize = Math.max(4, Math.min(baseCellSize, baseCellSize / 2)); // Simplified base size
            int effectiveRacerSize = Math.max(4, (int)(baseRacerSize * zoomLevel));

            g2d.fillOval(startPos.x - effectiveRacerSize / 2, startPos.y - effectiveRacerSize / 2, effectiveRacerSize, effectiveRacerSize);
        }

        private void drawTeleportMovement(Graphics2D g2d, TeleportEffect effect, Point from, Point to, double effectiveCellSize) {
            double progress = (double) (animationFrame - PATH_FRAMES) / (ANIMATION_FRAMES - PATH_FRAMES);
            progress = Math.max(0, Math.min(1, progress)); // Clamp progress

            Color racerColor = getRacerColor(effect.racerName);
            g2d.setColor(racerColor);
            float strokeWidth = Math.max(1f, (float)(3 * zoomLevel)); // Scale stroke
            g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Draw particles along the path
            int particleBaseSize = Math.max(2, (int)(8 * zoomLevel)); // Scale particle size
            for (int i = 0; i < 5; i++) {
                // Spread particles along the path, cycling
                double particleProgress = (progress + (double) i / 5.0) % 1.0;
                int px = (int) (from.x + (to.x - from.x) * particleProgress);
                int py = (int) (from.y + (to.y - from.y) * particleProgress);

                // Particles shrink as they move
                int size = (int) (particleBaseSize * (1.0 - particleProgress));
                if (size > 1) {
                    g2d.fillOval(px - size / 2, py - size / 2, size, size);
                }
            }

            // Draw flash at destination near the end
            if (progress > 0.8) {
                double flashProgress = (progress - 0.8) / 0.2; // 0.0 to 1.0
                int flashSize = Math.max(1, (int) (effectiveCellSize * flashProgress)); // Scale flash
                int alpha = (int) (200 * (1.0 - flashProgress)); // Fade out
                Color flashColor = new Color(255, 255, 255, Math.max(0, alpha));
                g2d.setColor(flashColor);
                g2d.fillOval(to.x - flashSize / 2, to.y - flashSize / 2, flashSize, flashSize);
            }
        }

        private void drawChargeEffect(Graphics2D g2d, ChargeEffect effect) {
            Point racerCenterPos = racerScreenPositions.get(effect.racerName);
            if (racerCenterPos == null) {
                racerCenterPos = gridToScreenCenter(effect.position); // Fallback if not found
            }

            double effectiveCellSize = getEffectiveCellSize();

            // Simple pulsing yellow circle animation
            float pulseProgress = (float) (animationFrame % ANIMATION_FRAMES) / ANIMATION_FRAMES; // 0.0 to 1.0 cycle
            float scale = 1.0f + 0.5f * (float) Math.sin(pulseProgress * Math.PI); // Expands and contracts
            int alpha = 150 - (int) (100 * Math.abs(pulseProgress - 0.5) * 2); // Fades in and out

            int baseSize = (int) (effectiveCellSize / 2); // Scale base size
            int currentSize = Math.max(1, (int) (baseSize * scale));
            int x = racerCenterPos.x - currentSize / 2;
            int y = racerCenterPos.y - currentSize / 2;

            Color chargeColor = new Color(255, 223, 0, Math.max(0, Math.min(255, alpha))); // Yellow, semi-transparent

            g2d.setColor(chargeColor);
            g2d.fillOval(x, y, currentSize, currentSize);

            // Optional: Add small sparks (scale sparks too)
            if (effectiveCellSize > 10) { // Only draw sparks if reasonably zoomed
                g2d.setColor(new Color(255, 255, 100, alpha)); // Lighter yellow
                int sparkCount = 5;
                int sparkLength = Math.max(2, (int)(5 * zoomLevel)); // Scale spark length
                for (int i = 0; i < sparkCount; i++) {
                    double angle = (pulseProgress * 2 * Math.PI) + (i * 2 * Math.PI / sparkCount);
                    int startX = racerCenterPos.x + (int) (baseSize / 2 * Math.cos(angle));
                    int startY = racerCenterPos.y + (int) (baseSize / 2 * Math.sin(angle));
                    int endX = startX + (int) (sparkLength * Math.cos(angle));
                    int endY = startY + (int) (sparkLength * Math.sin(angle));
                    g2d.drawLine(startX, startY, endX, endY);
                }
            }
        }

        // --- Coordinate Conversion ---
        private Point gridToScreen(Point gridPos) {
            double effectiveCellSize = getEffectiveCellSize();
            int screenX = LEFT_PADDING + (int) ((gridPos.x - viewOffset.x) * effectiveCellSize);
            int screenY = TOP_PADDING + (int) ((gridPos.y - viewOffset.y) * effectiveCellSize);
            return new Point(screenX, screenY);
        }

        private Point gridToScreenCenter(Point gridPos) {
            double effectiveCellSize = getEffectiveCellSize();
            int screenX = LEFT_PADDING + (int) ((gridPos.x - viewOffset.x + 0.5) * effectiveCellSize);
            int screenY = TOP_PADDING + (int) ((gridPos.y - viewOffset.y + 0.5) * effectiveCellSize);
            return new Point(screenX, screenY);
        }

        private Point2D.Double screenToGrid(Point screenPos) {
            double effectiveCellSize = getEffectiveCellSize();
            if (effectiveCellSize <= 0) {
                return new Point2D.Double(0, 0); // Avoid division by zero
            }
            double gridX = viewOffset.x + (double) (screenPos.x - LEFT_PADDING) / effectiveCellSize;
            double gridY = viewOffset.y + (double) (screenPos.y - TOP_PADDING) / effectiveCellSize;
            return new Point2D.Double(gridX, gridY);
        }

        private Color getRacerColor(String racerName) {
            int hashCode = racerName.hashCode();
            float hue = (float) (Math.abs(hashCode % 360)) / 360.0f;
            float saturation = 0.7f + (float) (Math.abs((hashCode / 360) % 30)) / 100.0f;
            float brightness = 0.8f + (float) (Math.abs((hashCode / 360 / 30) % 20)) / 100.0f;

            switch (racerName) {
                case "ScarletKnight": return Color.RED;
                case "StarbucksTruck": return new Color(0, 100, 0);
                case "LogNExpress": return Color.BLUE;
                case "NLogNExpress": return new Color(128, 0, 128);
                default: return Color.getHSBColor(hue, saturation, brightness);
            }
        }
    }

    static abstract class TurnEffect {
        String racerName;

        public TurnEffect(String racerName) {
            this.racerName = racerName;
        }
    }

    static class TeleportEffect extends TurnEffect {
        Point from;
        Point to;
        Point previousScreenPos;

        public TeleportEffect(String racerName, Point from, Point to, Point previousScreenPos) {
            super(racerName);
            this.from = from;
            this.to = to;
            this.previousScreenPos = previousScreenPos;
        }
    }

    static class ChargeEffect extends TurnEffect {
        Point position;

        public ChargeEffect(String racerName, Point position) {
            super(racerName);
            this.position = position;
        }
    }
}
