package simulation.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import simu.framework.Trace;
import simu.framework.Trace.Level;

import simulation.data.SimulationHistoryRepository;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MainApp extends Application implements ISimulatorUI {

    private static final long MIN_DELAY_MS = 5;
    private static final long MAX_DELAY_MS = 2000;
    private static final String ERROR_BORDER_STYLE = "-fx-border-color: #d32f2f; -fx-border-width: 2;";
    private static final double MIN_WINDOW_WIDTH = 1100;
    private static final double MIN_WINDOW_HEIGHT = 600;

    private IControllerVtoM controller;
    private TextField time;
    private TextField delay;
    private Slider delaySlider;
    private SimulationHistoryRepository historyRepository;
    private Label results;
    private Label totalServedValue;
    private Label avgWaitValue;
    private Label summaryValue;
    private Label statusValue;
    private Label currentSpeedLabel;
    private ListView<String> historyListView;
    private boolean updatingDelayField;
    private Button startButton;
    private Button slowButton;
    private Button speedUpButton;
    private Button pauseButton;
    private Button resumeButton;
    private Button stepButton;
    private IVisualisation display;

    @Override
    public void init() {
        Trace.setTraceLevel(Level.INFO);
        controller = new Controller(this);
        historyRepository = new SimulationHistoryRepository();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        startButton = new Button("Start / Restart");
        startButton.setTooltip(new Tooltip("Begin a new simulation run with the entered settings."));
        startButton.setOnAction(event -> {
            if (controller.isSimulationRunning()) {
                return;
            }
            if (validateInputs()) {
                controller.startSimulation();
                onSimulationStarted();
            }
        });

        slowButton = new Button("Slower animation");
        slowButton.setTooltip(new Tooltip("Increase delay by 10% to slow down visuals."));
        slowButton.setOnAction(e -> controller.decreaseSpeed());

        speedUpButton = new Button("Faster animation");
        speedUpButton.setTooltip(new Tooltip("Decrease delay by 10% to speed up visuals."));
        speedUpButton.setOnAction(e -> controller.increaseSpeed());

        pauseButton = new Button("Pause");
        pauseButton.setTooltip(new Tooltip("Temporarily halt progression while keeping the current state."));
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> {
            if (!controller.isSimulationRunning()) {
                return;
            }
            controller.pauseSimulation();
            onSimulationPaused();
        });

        resumeButton = new Button("Resume");
        resumeButton.setTooltip(new Tooltip("Continue the simulation after a pause."));
        resumeButton.setDisable(true);
        resumeButton.setOnAction(e -> {
            if (!controller.isSimulationRunning()) {
                return;
            }
            controller.resumeSimulation();
            onSimulationResumed();
        });

        stepButton = new Button("Step once");
        stepButton.setTooltip(new Tooltip("Execute a single simulation event for inspection."));
        stepButton.setDisable(true);
        stepButton.setOnAction(e -> {
            if (!controller.isSimulationRunning()) {
                if (!validateInputs()) {
                    return;
                }
                controller.startSimulation();
                onSimulationStarted();
            }
            controller.pauseSimulation();
            controller.stepSimulation();
            onSimulationPaused();
        });

        time = new TextField("200");
        time.setPrefWidth(150);
        time.setMaxWidth(150);
        time.setAlignment(Pos.CENTER_RIGHT);
        time.textProperty().addListener((obs, oldVal, newVal) -> validateTimeFieldImmediate());
        time.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                validateTimeFieldImmediate();
            }
        });
        delay = new TextField("10");
        delay.setPrefWidth(150);
        delay.setMaxWidth(150);
        delay.setAlignment(Pos.CENTER_RIGHT);
        delay.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDelayField) {
                return;
            }
            validateDelayFieldImmediate();
        });
        delay.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                enforceDelayBounds();
            }
        });
        delaySlider = new Slider(MIN_DELAY_MS, MAX_DELAY_MS, Double.parseDouble(delay.getText()));
        delaySlider.setShowTickLabels(true);
        delaySlider.setShowTickMarks(true);
        delaySlider.setMajorTickUnit(500);
        delaySlider.setMinorTickCount(4);
        delaySlider.setBlockIncrement(10);
        delaySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDelayField) {
                return;
            }
            handleDelaySliderChange(Math.round(newVal.doubleValue()));
        });
        results = new Label();
        totalServedValue = new Label("-");
        avgWaitValue = new Label("-");
        summaryValue = new Label("Run a simulation to see the summary.");
        summaryValue.setWrapText(true);
        summaryValue.setMaxWidth(220);
        statusValue = new Label("Idle");
        statusValue.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        currentSpeedLabel = new Label();
        currentSpeedLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 13));
        currentSpeedLabel.setStyle("-fx-text-fill: #333333;");
        historyListView = new ListView<>();
        historyListView.setPrefHeight(180);
        historyListView.setFocusTraversable(false);
        historyListView.setPlaceholder(new Label("No previous runs yet."));

        Visualisation visualCanvas = new Visualisation(450, 250);
        display = visualCanvas;
        StackPane visualisationContainer = new StackPane(visualCanvas);
        visualisationContainer.setMinWidth(400);
        visualisationContainer.setMinHeight(500);
        visualisationContainer.setPrefHeight(500);
        visualisationContainer.setMaxHeight(500);
        visualisationContainer.widthProperty().addListener(
                (obs, oldV, newV) -> visualCanvas.resizeCanvas(newV.doubleValue(), 500));
        visualisationContainer.heightProperty().addListener(
                (obs, oldV, newV) -> visualCanvas.resizeCanvas(visualisationContainer.getWidth(), 500));

        Label description = new Label(
                "Simulates a single coffee shop where customers move through cashier, barista, finishing, and pickup stages.");
        description.setWrapText(true);
        description.setMaxWidth(260);

        Label settingsTitle = new Label("Simulation Settings");
        settingsTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        Label timeLabel = new Label("How long to simulate (min):");
        timeLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        Label delayLabel = new Label("Animation speed (ms):");
        delayLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        Label animationHint = new Label("Higher value = slower movement updates.");
        animationHint.setFont(Font.font("Tahoma", FontWeight.NORMAL, 11));
        animationHint.setWrapText(true);
        animationHint.setMaxWidth(180);

        GridPane settingsGrid = new GridPane();
        settingsGrid.setVgap(14);
        settingsGrid.setHgap(10);
        settingsGrid.setPadding(new Insets(8, 0, 12, 0));
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setHalignment(HPos.RIGHT);
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setPrefWidth(150);
        fieldColumn.setHgrow(Priority.ALWAYS);
        settingsGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);
        settingsGrid.add(timeLabel, 0, 0);
        settingsGrid.add(time, 1, 0);
        settingsGrid.add(delayLabel, 0, 1);
        settingsGrid.add(delay, 1, 1);
        settingsGrid.add(delaySlider, 1, 2);
        settingsGrid.add(animationHint, 0, 3, 2, 1);

        Label controlsTitle = new Label("Controls");
        controlsTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        Label stateControlsLabel = new Label("Simulation state");
        stateControlsLabel.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        HBox stateButtonsRow = new HBox(10, startButton, pauseButton, resumeButton);
        stateButtonsRow.setAlignment(Pos.CENTER_LEFT);

        HBox stepRow = new HBox(stepButton);
        stepRow.setAlignment(Pos.CENTER_LEFT);

        Label speedControlsLabel = new Label("Animation speed");
        speedControlsLabel.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 13));
        HBox speedButtonsRow = new HBox(10, speedUpButton, slowButton);
        speedButtonsRow.setAlignment(Pos.CENTER_LEFT);

        VBox controlsSection = new VBox(10);
        controlsSection.setPadding(new Insets(5, 0, 12, 0));
        controlsSection.getChildren().addAll(
                stateControlsLabel,
                stateButtonsRow,
                stepRow,
                speedControlsLabel,
                speedButtonsRow,
                currentSpeedLabel);

        Label resultsTitle = new Label("Results");
        resultsTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        Label totalLabel = new Label("Simulation finished after (min):");
        totalLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        Label totalServedLabel = new Label("Total customers served:");
        totalServedLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        Label avgWaitLabel = new Label("Average waiting time (min):");
        avgWaitLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        Label summaryLabel = new Label("Summary:");
        summaryLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        Label statusLabel = new Label("Status:");
        statusLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        GridPane resultsGrid = new GridPane();
        resultsGrid.setAlignment(Pos.TOP_LEFT);
        resultsGrid.setPadding(new Insets(5, 0, 0, 0));
        resultsGrid.setHgap(10);
        resultsGrid.setVgap(8);
        resultsGrid.add(totalLabel, 0, 0);
        resultsGrid.add(results, 1, 0);
        resultsGrid.add(totalServedLabel, 0, 1);
        resultsGrid.add(totalServedValue, 1, 1);
        resultsGrid.add(avgWaitLabel, 0, 2);
        resultsGrid.add(avgWaitValue, 1, 2);
        resultsGrid.add(summaryLabel, 0, 3);
        resultsGrid.add(summaryValue, 1, 3);
        resultsGrid.add(statusLabel, 0, 4);
        resultsGrid.add(statusValue, 1, 4);

        VBox leftPane = new VBox(16);
        leftPane.setPadding(new Insets(15));
        leftPane.setAlignment(Pos.TOP_LEFT);
        Label historyTitle = new Label("History");
        historyTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));

        leftPane.getChildren()
                .addAll(description, settingsTitle, settingsGrid, controlsTitle, controlsSection, resultsTitle,
                        resultsGrid, historyTitle, historyListView);

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(15));
        hBox.setSpacing(10);
        hBox.getChildren().addAll(leftPane, visualisationContainer);
        HBox.setHgrow(visualisationContainer, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(hBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Coffee Shop Simulation");
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.show();

        onSimulationStopped();
        updateStatus("Idle");
        validateDelayFieldImmediate();
        refreshHistoryViewAsync();
    }

    @Override
    public double getTime() {
        return Double.parseDouble(time.getText());
    }

    @Override
    public long getDelay() {
        return Long.parseLong(delay.getText());
    }

    @Override
    public void setEndingTime(double time) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        results.setText(formatter.format(time));
        updateStatus("Finished");
        onSimulationStopped();
    }

    @Override
    public IVisualisation getVisualisation() {
        return display;
    }

    @Override
    public void showCurrentDelay(long delayValue) {
        Platform.runLater(() -> updateDelayControls(delayValue));
    }

    @Override
    public void showSummary(SimulationSummary summary) {
        setEndingTime(summary.simulationTime());
        totalServedValue.setText(String.valueOf(summary.totalCustomersServed()));
        avgWaitValue.setText(summary.formattedAverageWaitingTime());
        summaryValue.setText(summary.toSummaryLine());
        appendSummaryAsync(summary);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void onSimulationStarted() {
        startButton.setDisable(true);
        slowButton.setDisable(false);
        speedUpButton.setDisable(false);
        pauseButton.setDisable(false);
        resumeButton.setDisable(true);
        stepButton.setDisable(true);
        updateStatus("Running");
    }

    private void onSimulationPaused() {
        pauseButton.setDisable(true);
        resumeButton.setDisable(false);
        stepButton.setDisable(false);
        slowButton.setDisable(false);
        speedUpButton.setDisable(false);
        updateStatus("Paused");
    }

    private void onSimulationResumed() {
        pauseButton.setDisable(false);
        resumeButton.setDisable(true);
        stepButton.setDisable(true);
        slowButton.setDisable(false);
        speedUpButton.setDisable(false);
        updateStatus("Running");
    }

    private void onSimulationStopped() {
        startButton.setDisable(false);
        slowButton.setDisable(true);
        speedUpButton.setDisable(true);
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        stepButton.setDisable(true);
    }

    private void updateStatus(String state) {
        statusValue.setText(state);
    }

    private void handleDelaySliderChange(long sliderValue) {
        updatingDelayField = true;
        delay.setText(String.valueOf(sliderValue));
        updatingDelayField = false;
        currentSpeedLabel.setText(formatDelayText(sliderValue));
        if (controller != null && controller.isSimulationRunning()) {
            controller.setDelay(sliderValue);
        }
    }

    private void updateDelayControls(long delayValue) {
        updatingDelayField = true;
        delay.setText(String.valueOf(delayValue));
        delaySlider.setValue(delayValue);
        updatingDelayField = false;
        setFieldValid(delay);
        currentSpeedLabel.setText(formatDelayText(delayValue));
    }

    private String formatDelayText(long delayValue) {
        return delayValue + " ms per step";
    }

    private void appendSummaryAsync(SimulationSummary summary) {
        CompletableFuture.runAsync(() -> {
            try {
                historyRepository.append(summary);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).whenComplete((unused, throwable) -> {
            if (throwable != null) {
                Platform.runLater(() -> summaryValue.setText("Saved locally, but history write failed"));
            } else {
                refreshHistoryViewAsync();
            }
        });
    }

    private void refreshHistoryViewAsync() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return historyRepository.readAll();
            } catch (IOException e) {
                return List.<SimulationHistoryRepository.SimulationHistoryEntry>of();
            }
        }).thenAccept(entries -> Platform.runLater(() -> {
            if (entries.isEmpty()) {
                historyListView.getItems().clear();
                return;
            }
            List<String> formatted = entries.stream()
                    .filter(entry -> !entry.isEmpty())
                    .map(SimulationHistoryRepository.SimulationHistoryEntry::toDisplayString)
                    .collect(Collectors.toList());
            historyListView.getItems().setAll(formatted);
        }));
    }

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        clearValidationStyles();

        parsePositiveDouble(time, "How long to simulate (min)", errors);
        Long delayValue = parsePositiveLong(delay, "Animation speed (ms)", errors);

        if (delayValue != null && (delayValue < MIN_DELAY_MS || delayValue > MAX_DELAY_MS)) {
            errors.append(String.format("Animation speed must be between %d and %d ms.%n", MIN_DELAY_MS, MAX_DELAY_MS));
            markInvalid(delay);
        }

        if (errors.length() > 0) {
            showValidationError(errors.toString());
            return false;
        }

        if (delayValue != null) {
            long clamped = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, delayValue));
            updateDelayControls(clamped);
        }

        return true;
    }

    private Double parsePositiveDouble(TextField field, String fieldLabel, StringBuilder errors) {
        try {
            double parsed = Double.parseDouble(field.getText());
            if (parsed <= 0) {
                errors.append(fieldLabel).append(" must be greater than zero.\n");
                markInvalid(field);
                return null;
            }
            setFieldValid(field);
            return parsed;
        } catch (NumberFormatException ex) {
            errors.append(fieldLabel).append(" must be a number.\n");
            markInvalid(field);
            return null;
        }
    }

    private Long parsePositiveLong(TextField field, String fieldLabel, StringBuilder errors) {
        try {
            long parsed = Long.parseLong(field.getText());
            if (parsed <= 0) {
                errors.append(fieldLabel).append(" must be greater than zero.\n");
                markInvalid(field);
                return null;
            }
            setFieldValid(field);
            return parsed;
        } catch (NumberFormatException ex) {
            errors.append(fieldLabel).append(" must be an integer.\n");
            markInvalid(field);
            return null;
        }
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Invalid input");
        alert.setHeaderText("Please correct the invalid values");
        alert.setContentText(message.trim());
        alert.showAndWait();
    }

    private void clearValidationStyles() {
        time.setStyle("");
        delay.setStyle("");
    }

    private void markInvalid(TextField field) {
        field.setStyle(ERROR_BORDER_STYLE);
    }

    private void setFieldValid(TextField field) {
        field.setStyle("");
    }

    private void validateTimeFieldImmediate() {
        parsePositiveDouble(time, "How long to simulate (min)", new StringBuilder());
    }

    private void validateDelayFieldImmediate() {
        StringBuilder tmp = new StringBuilder();
        Long value = parsePositiveLong(delay, "Animation speed (ms)", tmp);
        if (value == null) {
            currentSpeedLabel.setText("Enter animation speed (ms)");
            return;
        }
        if (value < MIN_DELAY_MS || value > MAX_DELAY_MS) {
            markInvalid(delay);
            currentSpeedLabel.setText(String.format("Allowed range: %d-%d ms", MIN_DELAY_MS, MAX_DELAY_MS));
        } else {
            setFieldValid(delay);
            currentSpeedLabel.setText(formatDelayText(value));
        }
    }

    private void enforceDelayBounds() {
        String text = delay.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        try {
            long parsed = Long.parseLong(text);
            long clamped = Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, parsed));
            updateDelayControls(clamped);
        } catch (NumberFormatException ignored) {
        }
    }
}
