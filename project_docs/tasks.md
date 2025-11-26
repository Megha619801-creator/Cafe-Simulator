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
  - [ ] Optionally create an `ArrivalProcess` class that owns a random generator and adds new arrival events to the event list.

### 3. Configurability and External Data
- [ ] **Make model parameters configurable**
  - [ ] Define a clear set of input parameters (arrival rates, service-time means, simulation end time, etc.).
  - [ ] Load these parameters either from a configuration file or later from the GUI.
- [ ] **Implement `FileManager`**
  - [ ] Read configuration data (parameters, scenarios) from a file.
  - [ ] Write simulation results and statistics (per service point and system-wide) to an output file (e.g., CSV or text).

### 4. Performance Metrics
- [ ] **Collect performance variables (per service point)**
  - [ ] Count arrivals `A`, completed customers `C`, and accumulate busy time `B`.
  - [ ] Track total simulation time `T` via `Clock`.
- [ ] **Compute derived measures (per `1.4_Performance_Variables.md`)**
  - [ ] Utilization `U = B / T`.
  - [ ] Throughput `X = C / T`.
  - [ ] Average service time `S = B / C`.
  - [ ] Average response time `R = W / C`, where `W` is the sum of individual response times.
  - [ ] Average number in system `N = W / T`.
  - [ ] Print or return these values at the end of the simulation.

### 5. JavaFX User Interface
- [ ] **Basic UI shell**
  - [ ] Implement `MainApp` as a JavaFX `Application` and create the primary `Stage` / `Scene`.
  - [ ] Create a `Controller` (and optional FXML) with input controls for model parameters and buttons for controlling the simulation (e.g., Start, Stop/Reset).
- [ ] **Displaying results**
  - [ ] Show key performance variables in the UI (tables, labels, or text area).
  - [ ] Provide clear labels and units so a casual user understands the outputs.

### 6. Visualisation and Animation
- [ ] **Canvas-based visualisation**
  - [ ] Create a dedicated Canvas (or Canvas-based class) that visualises customers, queues, and/or service points.
  - [ ] Use `Platform.runLater` from the simulation thread to update the Canvas safely.
- [ ] **Animation controls**
  - [ ] Allow slowing down/speeding up the simulation (e.g., via sleep intervals or step size).
  - [ ] Optionally support step-by-step execution (advance by one event or small time window).

### 7. Usability and External Behaviour
- [ ] **Runtime control**
  - [ ] Start, pause, resume, and stop the simulation from the UI.
  - [ ] Ensure the simulator runs autonomously once started, but reacts to UI controls.
- [ ] **User-friendly interface**
  - [ ] Choose readable fonts and colours.
  - [ ] Keep layout simple and intuitive for non-technical users.

### 8. Testing and Documentation
- [ ] **Automated and manual testing**
  - [ ] Add small JUnit tests or test programs for critical classes (`Clock`, `EventList`, `ServicePoint`, `ArrivalProcess`, etc.).
  - [ ] Manually verify that statistics and visualisation match expectations for simple scenarios.
- [ ] **Javadoc and project report**
  - [ ] Write Javadoc for all model classes and their methods/attributes (including private ones).
  - [ ] Maintain the written project document using the provided `ProjectWorkDocumentTemplate`, making sure it stays in sync with the implemented simulator.


