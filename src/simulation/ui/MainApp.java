package simulation.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import simu.framework.Trace;
import simu.framework.Trace.Level;

import java.text.DecimalFormat;

public class MainApp extends Application implements ISimulatorUI {

    private IControllerVtoM controller;
    private TextField time;
    private TextField delay;
    private Label results;
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
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        startButton = new Button("Start simulation");
        startButton.setOnAction(event -> {
            if (controller.isSimulationRunning()) {
                return;
            }
            controller.startSimulation();
            onSimulationStarted();
        });

        slowButton = new Button("Slow down");
        slowButton.setOnAction(e -> controller.decreaseSpeed());

        speedUpButton = new Button("Speed up");
        speedUpButton.setOnAction(e -> controller.increaseSpeed());

        pauseButton = new Button("Pause");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> {
            if (!controller.isSimulationRunning()) {
                return;
            }
            controller.pauseSimulation();
            onSimulationPaused();
        });

        resumeButton = new Button("Resume");
        resumeButton.setDisable(true);
        resumeButton.setOnAction(e -> {
            if (!controller.isSimulationRunning()) {
                return;
            }
            controller.resumeSimulation();
            onSimulationResumed();
        });

        stepButton = new Button("Step");
        stepButton.setDisable(true);
        stepButton.setOnAction(e -> {
            if (!controller.isSimulationRunning()) {
                controller.startSimulation();
                onSimulationStarted();
            }
            controller.pauseSimulation();
            controller.stepSimulation();
            onSimulationPaused();
        });

        time = new TextField("200");
        delay = new TextField("10");
        results = new Label();

        display = new Visualisation(450, 250);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(5);

        Label timeLabel = new Label("Simulation time:");
        timeLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        Label delayLabel = new Label("Delay:");
        delayLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        Label totalLabel = new Label("Total time:");
        totalLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));

        grid.add(timeLabel, 0, 0);
        grid.add(time, 1, 0);
        grid.add(delayLabel, 0, 1);
        grid.add(delay, 1, 1);
        grid.add(totalLabel, 0, 2);
        grid.add(results, 1, 2);
        grid.add(startButton, 0, 3);
        grid.add(speedUpButton, 0, 4);
        grid.add(slowButton, 1, 4);
        grid.add(pauseButton, 0, 5);
        grid.add(resumeButton, 1, 5);
        grid.add(stepButton, 0, 6);

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(15));
        hBox.setSpacing(10);
        hBox.getChildren().addAll(grid, (Canvas) display);

        Scene scene = new Scene(hBox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Coffee Shop Simulation");
        primaryStage.show();

        onSimulationStopped();
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
        onSimulationStopped();
    }

    @Override
    public IVisualisation getVisualisation() {
        return display;
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
    }

    private void onSimulationPaused() {
        pauseButton.setDisable(true);
        resumeButton.setDisable(false);
        stepButton.setDisable(false);
        slowButton.setDisable(false);
        speedUpButton.setDisable(false);
    }

    private void onSimulationResumed() {
        pauseButton.setDisable(false);
        resumeButton.setDisable(true);
        stepButton.setDisable(true);
        slowButton.setDisable(false);
        speedUpButton.setDisable(false);
    }

    private void onSimulationStopped() {
        startButton.setDisable(false);
        slowButton.setDisable(true);
        speedUpButton.setDisable(true);
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        stepButton.setDisable(true);
    }
}
