## Coffee Simulator – Project Task List

### 1. Core Architecture and Model
- [ ] **Introduce global simulation clock**
  - [x] Implement `Clock` as a Singleton (single instance for the whole simulator).
  - [x] Replace local time variables in the simulator logic with calls to `Clock`.
- [ ] **Refine event-driven engine**
  - [x] Ensure the main loop follows the three-phase simulation idea (A-, B-, C-phases) from the course material.
  - [x] Keep event handling (`ARRIVAL`, `DEPARTURE`, routing) clearly separated from UI code.

### 2. Randomness and Distributions
- [ ] **Integrate `eduni.distributions` library**
  - [x] Add the `eduni.distributions` sources/JAR to the project build so they are available on the classpath.
  - [x] Verify that demo code from the course material using `eduni.distributions` compiles and runs.
- [ ] **Use distribution objects instead of manual formulas**
  - [x] Replace manual exponential sampling (`-mean * Math.log(1 - rand.nextDouble())`) with `ContinuousGenerator` implementations (e.g., `Negexp`).
  - [x] Allow different service points to use different distributions and/or parameters.
  - [x] Optionally create an `ArrivalProcess` class that owns a random generator and adds new arrival events to the event list.

### 3. Configurability and External Data
- [ ] **Make model parameters configurable**
  - [x] Define a clear set of input parameters (arrival rates, service-time means, simulation end time, etc.).
  - [x] Load these parameters either from a configuration file or later from the GUI.
- [ ] **Implement `FileManager`**
  - [x] Read configuration data (parameters, scenarios) from a file.
  - [x] Write simulation results and statistics (per service point and system-wide) to an output file (e.g., CSV or text).

### 4. Performance Metrics
- [ ] **Collect performance variables (per service point)**
  - [x] Count arrivals `A`, completed customers `C`, and accumulate busy time `B`.
  - [x] Track total simulation time `T` via `Clock`.
- [ ] **Compute derived measures (per `1.4_Performance_Variables.md`)**
  - [x] Utilization `U = B / T`.
  - [x] Throughput `X = C / T`.
  - [x] Average service time `S = B / C`.
  - [x] Average response time `R = W / C`, where `W` is the sum of individual response times.
  - [x] Average number in system `N = W / T`.
  - [x] Print or return these values at the end of the simulation.

### 5. JavaFX User Interface
- [ ] **Basic UI shell**
  - [x] Implement `MainApp` as a JavaFX `Application` and create the primary `Stage` / `Scene`.
  - [x] Create a `Controller` (and optional FXML) with input controls for model parameters and buttons for controlling the simulation (e.g., Start, Stop/Reset).
- [ ] **Displaying results**
  - [x] Show key performance variables in the UI (tables, labels, or text area).
  - [x] Provide clear labels and units so a casual user understands the outputs.

### 6. Visualisation and Animation
- [ ] **Canvas-based visualisation**
  - [x] Create a dedicated Canvas (or Canvas-based class) that visualises customers, queues, and/or service points.
  - [x] Use `Platform.runLater` from the simulation thread to update the Canvas safely.
- [ ] **Animation controls**
  - [x] Allow slowing down/speeding up the simulation (e.g., via sleep intervals or step size).
  - [x] Optionally support step-by-step execution (advance by one event or small time window).

### 7. Usability and External Behaviour
- [ ] **Runtime control**
  - [x] Start, pause, resume, and stop the simulation from the UI.
  - [x] Ensure the simulator runs autonomously once started, but reacts to UI controls.
- [ ] **User-friendly interface**
  - [x] Choose readable fonts and colours.
  - [x] Keep layout simple and intuitive for non-technical users.

### 7.1 UI Improvement Tasks
- [ ] **Improve terminology and labels**
  - [x] Rename technical labels to user-friendly names (e.g., “Simulation time” → “How long to simulate (min)”).
  - [x] Rename “Delay” to “Animation speed (ms)” and add a short explanation.
  - [x] Add a short description text explaining what the simulator does.

- [ ] **Improve layout and grouping**
  - [x] Group settings into blocks: Simulation Settings, Controls, and Results.
  - [x] Add spacing and align input fields for a cleaner layout.
  - [x] Add a status label (Idle / Running / Paused / Finished).

- [ ] **Improve animation controls**
  - [x] Add clearer buttons for speed control and state transitions.
  - [x] Consider replacing numeric delay input with a Slider for animation speed.

- [ ] **Input validation**
  - [x] Validate numeric input for simulation time and delay.
  - [x] Show an Alert window when invalid values are entered.
  - [ ] Highlight invalid fields with a red border.

- [ ] **Visualisation improvements**
  - [ ] Add a legend explaining the meaning of labels like IN / MB.
  - [ ] Adjust colours for better contrast and readability.
  - [ ] Improve role tooltips (Cashier, Barista, etc.) for non-technical users.

- [ ] **Enhanced results display**
  - [ ] Add “Total customers served”.
  - [ ] Add “Average waiting time”.
  - [ ] Add simplified readable summaries (e.g., “123 customers, avg wait 1.8 min”).

### 8. Testing and Documentation
- [ ] **Automated and manual testing**
  - [ ] Add small JUnit tests or test programs for critical classes (`Clock`, `EventList`, `ServicePoint`, `ArrivalProcess`, etc.).
  - [ ] Manually verify that statistics and visualisation match expectations for simple scenarios.
- [ ] **Javadoc and project report**
  - [ ] Write Javadoc for all model classes and their methods/attributes (including private ones).
  - [ ] Maintain the written project document using the provided `ProjectWorkDocumentTemplate`, making sure it stays in sync with the implemented simulator.
