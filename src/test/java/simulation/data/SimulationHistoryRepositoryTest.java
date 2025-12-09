package simulation.data;

import org.junit.jupiter.api.*;
import simulation.ui.SimulationSummary;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimulationHistoryRepositoryTest {

    private SimulationHistoryRepository repo;
    private static final Path TEST_FILE = Path.of("output", "simulation-history.csv");

    @BeforeEach
    void setup() throws IOException {
        repo = new SimulationHistoryRepository();
        // Delete any existing test file before running each test
        if (Files.exists(TEST_FILE)) {
            Files.delete(TEST_FILE);
        }
    }

    @Test
    void appendAndReadSingleEntry() throws IOException {
        SimulationSummary summary = new SimulationSummary(5.5, 12, 1.75);

        repo.append(summary);
        List<SimulationHistoryRepository.SimulationHistoryEntry> entries = repo.readAll();

        assertEquals(1, entries.size(), "CSV should contain exactly one entry");

        SimulationHistoryRepository.SimulationHistoryEntry entry = entries.get(0);

        assertFalse(entry.isEmpty(), "Entry should not be empty");
        assertEquals(12, entry.summary().totalCustomersServed());
        assertEquals(5.5, entry.summary().simulationTime(), 1e-9);
        assertEquals(1.75, entry.summary().averageWaitingTime(), 1e-9);
    }

    @Test
    void readEmptyWhenFileMissing() throws IOException {
        // Ensure file does not exist
        if (Files.exists(TEST_FILE)) Files.delete(TEST_FILE);

        List<SimulationHistoryRepository.SimulationHistoryEntry> entries = repo.readAll();

        assertTrue(entries.isEmpty(), "Reading nonexistent file should return empty list");
    }

    @Test
    void parseInvalidLineReturnsEmpty() throws IOException {
        // Create corrupted file manually
        Files.createDirectories(TEST_FILE.getParent());
        Files.writeString(TEST_FILE,
                "timestamp;simulationTime;totalCustomers;averageWaiting\n" + // header
                        "INVALID DATA LINE\n",
                StandardOpenOption.CREATE);

        List<SimulationHistoryRepository.SimulationHistoryEntry> entries = repo.readAll();

        assertEquals(1, entries.size());
        assertTrue(entries.get(0).isEmpty(), "Invalid CSV line should produce empty entry");
    }

}
