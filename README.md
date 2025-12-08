# Cafe Simulator

A Java-based Coffee Simulator that models a coffee shop’s customer arrivals, barista workflows, and service behavior to measure wait times, throughput, and resource utilization. The simulation helps experiment with staffing, arrival rates, scheduling and machine use to compare operational scenarios and observe key metrics.


---

## Quick summary (simple, professional)
I built a Coffee Simulator in Java that reproduces how customers, baristas, and coffee machines interact over time. The simulator advances time, handles queues, schedules service events, and records metrics such as average wait time, throughput, and resource utilization. It is modular so scenarios (arrival patterns, number of baristas, service-time distributions) can be changed and compared.

---

## What I used (technologies and tools)
- Language: Java (100%)
- Build tools: Maven 
- Testing: JUnit
- Logging: SLF4J / Logback (or java.util.logging)
- Data export: CSV output support for metrics and traces
- Development: JDK 11+ 

---

## What we did (our contributions — plain and professional)
- Implemented the core discrete-event simulation engine in Java to advance simulated time, manage event queues, and process service events.
- Created agent models: Customer, Barista, and Machine with configurable behaviors and state.
- Implemented customer arrival processes and service-time distributions (configurable parameters).
- Built task scheduling and service logic to handle queuing, service start/end, and interruption/resumption .
- Implemented a points/logging system and CSV output for metrics: average wait time, total served, throughput, utilization, and other key indicators.
- Designed a modular code layout so components (arrival generators, service policies, output handlers) are easy to extend or replace.
- Wrote unit tests around core simulation components and example scenario runners to validate reproducibility.
- Added documentation (setup, running examples, project structure) so other developers can run and extend the simulator.

---

## Main features
- Discrete-event simulation core (time-stepped / event-driven).
- Configurable arrival models (poisson, fixed-rate, or custom).
- Configurable service-time distributions (exponential, normal, fixed).
- Multiple resource types (baristas, machines) and utilization tracking.
- Queue management and service policies (FIFO; extendable).
- Scenario runner with CSV outputs for later analysis.
- Unit tests and example scenarios.

---

## Quick start (development & run examples)

## Notes for maintainers
- Ensure the correct JDK version is installed and JAVA_HOME is set.


