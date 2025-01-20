package com.kiralyzoltan.rtree;

import com.kiralyzoltan.rtree.config.AppInstanceConfig;
import com.kiralyzoltan.rtree.history.History;
import com.kiralyzoltan.rtree.history.HistoryMapper;
import com.kiralyzoltan.rtree.history.HistoryRepository;
import com.kiralyzoltan.rtree.history.HistoryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RTreeServiceTests {

    private final String TEST_DIR = "test";
    private Path tempDirToDelete;

    @Mock
    private AppInstanceConfig appInstanceConfig;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private HistoryMapper historyMapper;

    private RTreeService rTreeService;

    @BeforeEach
    void setUp() {
        rTreeService = new RTreeService(appInstanceConfig, historyRepository, historyMapper);
    }

    @AfterEach
    void cleanUp() throws IOException {
        if (tempDirToDelete != null &&Files.exists(this.tempDirToDelete)) {
            FileSystemUtils.deleteRecursively(this.tempDirToDelete);
        }
    }

    @Test
    void getUniqueFilenames_WithValidPath_ReturnsUniqueFiles() throws Exception {
        // Arrange
        Path tempDir = Files.createTempDirectory(TEST_DIR);
        tempDirToDelete = tempDir;
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));
        Files.createFile(tempDir.resolve("file3.txt"));

        when(appInstanceConfig.getInstanceName()).thenReturn("test-instance");

        // Act
        List<String> result = rTreeService.getUniqueFilenamesAndSaveHistory(tempDir.toString(), Optional.empty());

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains("file1.txt"));
        assertTrue(result.contains("file2.txt"));
        assertTrue(result.contains("file3.txt"));
        verify(historyRepository).save(any(History.class));
    }

    @Test
    void getUniqueFilenames_WithNestedValidPath_ReturnsUniqueFiles() throws Exception {
        // Arrange
        Path tempDir = Files.createTempDirectory(TEST_DIR);
        tempDirToDelete = tempDir;
        Path subDir1 = Files.createDirectory(tempDir.resolve("subDir1"));
        Path subDir2 = Files.createDirectory(tempDir.resolve("subDir2"));
        Path subDir3 = Files.createDirectory(tempDir.resolve("subDir3"));
        Path subDir4 = Files.createDirectory(tempDir.resolve("subDir4"));
        Path subDir5 = Files.createDirectory(tempDir.resolve("subDir5"));
        // file1 and file4 are unique
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(subDir1.resolve("file2.txt"));
        Files.createFile(subDir2.resolve("file2.txt"));
        Files.createFile(subDir3.resolve("file3.txt"));
        Files.createFile(subDir4.resolve("file3.txt"));
        Files.createFile(subDir5.resolve("file4.txt"));

        when(appInstanceConfig.getInstanceName()).thenReturn("test-instance");

        // Act
        List<String> result = rTreeService.getUniqueFilenamesAndSaveHistory(tempDir.toString(), Optional.empty());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("file1.txt"));
        assertTrue(result.contains("file4.txt"));
        verify(historyRepository).save(any(History.class));
    }

    @Test
    void getUniqueFilenames_WithValidPath_ReturnsUniqueFiles_EvenIfFileOccursOddTimes() throws IOException {
        // Arrange
        Path tempDir = Files.createTempDirectory(TEST_DIR);
        tempDirToDelete = tempDir;
        Path subDir1 = Files.createDirectory(tempDir.resolve("subDir1"));
        Path subDir2 = Files.createDirectory(tempDir.resolve("subDir2"));

        // file1 and file4 are unique
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));
        Files.createFile(subDir1.resolve("file2.txt"));
        Files.createFile(subDir2.resolve("file2.txt"));

        when(appInstanceConfig.getInstanceName()).thenReturn("test-instance");

        // Act
        List<String> result = rTreeService.getUniqueFilenamesAndSaveHistory(tempDir.toString(), Optional.empty());

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.contains("file1.txt"));
        verify(historyRepository).save(any(History.class));
    }

    @Test
    void getUniqueFilenames_WithDuplicates_ReturnsOnlyUniqueFiles() throws Exception {
        // Arrange
        Path tempDir = Files.createTempDirectory(TEST_DIR);
        tempDirToDelete = tempDir;
        Path subDir = Files.createDirectory(tempDir.resolve("a"));
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));
        Files.createFile(subDir.resolve("file2.txt"));
        Files.createFile(subDir.resolve("file3.txt"));

        when(appInstanceConfig.getInstanceName()).thenReturn("test-instance");

        // Act
        List<String> result = rTreeService.getUniqueFilenamesAndSaveHistory(tempDir.toString(), Optional.empty());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("file1.txt"));
        assertTrue(result.contains("file3.txt"));
        verify(historyRepository).save(any(History.class));
    }

    @Test
    void getUniqueFilenames_WithExtension_ReturnsOnlyFilesWithGivenExtension() throws Exception {
        // Arrange
        Path tempDir = Files.createTempDirectory(TEST_DIR);
        tempDirToDelete = tempDir;
        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));
        Files.createFile(tempDir.resolve("file3.json"));
        Files.createFile(tempDir.resolve("file4.json"));
        Files.createFile(tempDir.resolve("file5.yaml"));

        when(appInstanceConfig.getInstanceName()).thenReturn("test-instance");

        // Act
        List<String> result = rTreeService.getUniqueFilenamesAndSaveHistory(tempDir.toString(), Optional.of("json"));

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains("file3.json"));
        assertTrue(result.contains("file4.json"));
        verify(historyRepository).save(any(History.class));
    }

    @Test
    void getUniqueFilenames_WithInvalidPath_ThrowsNoSuchFileException() {
        assertThrows(NoSuchFileException.class, () -> {
            // Act
            rTreeService.getUniqueFilenamesAndSaveHistory("invalid-path",Optional.empty());
        });
    }

    @Test
    @SuppressWarnings("unchecked") // We know that the mock is of type JpaSpecificationExecutor<History>
    void getHistory_WithAllParameters_ReturnsResults() {
        // Arrange
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        History history = new History("test-user", timestamp, "test-data");
        HistoryResponse historyResponse = new HistoryResponse("test-user", timestamp, "test-data");

        when(historyRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(history));
        when(historyMapper.toResponse(history))
                .thenReturn(historyResponse);

        // Act
        List<HistoryResponse> result = rTreeService.getHistory(
                Optional.of("test-user"),
                Optional.of(timestamp),
                Optional.of("test-data")
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("test-user", result.getFirst().username());
        assertEquals(timestamp, result.getFirst().createdAt());
        assertEquals("test-data", result.getFirst().jsonData());
        verify(historyRepository).findAll(any(Specification.class));
        verify(historyMapper).toResponse(any(History.class));
    }

    @Test
    @SuppressWarnings("unchecked") // We know that the mock is of type JpaSpecificationExecutor<History>
    void getHistory_WithUsernameSet_ReturnsMultipleResults() {
        // Arrange
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        History history = new History("test-user", timestamp, "test-data");
        History history2 = new History("test-user", timestamp, "test-data2");
        HistoryResponse historyResponse = new HistoryResponse("test-user", timestamp, "test-data");
        HistoryResponse historyResponse2 = new HistoryResponse("test-user", timestamp, "test-data2");

        when(historyRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(history, history2));
        when(historyMapper.toResponse(history))
                .thenReturn(historyResponse);
        when(historyMapper.toResponse(history2))
                .thenReturn(historyResponse2);

        // Act
        List<HistoryResponse> result = rTreeService.getHistory(
                Optional.of("test-user"),
                Optional.empty(),
                Optional.empty());

        // Assert
        assertEquals(2, result.size());
        assertEquals("test-user", result.getFirst().username());
        assertEquals(timestamp, result.getFirst().createdAt());
        assertEquals("test-data", result.getFirst().jsonData());
        assertEquals("test-user", result.get(1).username());
        assertEquals(timestamp, result.get(1).createdAt());
        assertEquals("test-data2", result.get(1).jsonData());
        verify(historyRepository).findAll(any(Specification.class));
        verify(historyMapper, times(2)).toResponse(any(History.class));
    }

    @Test
    @SuppressWarnings("unchecked") // We know that the mock is of type JpaSpecificationExecutor<History>
    void getHistory_WithTimestampSet_ReturnsMultipleResults() {
        // Arrange
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        History history = new History("test-user", timestamp, "test-data");
        History history2 = new History("test-user", timestamp, "test-data2");
        HistoryResponse historyResponse = new HistoryResponse("test-user", timestamp, "test-data");
        HistoryResponse historyResponse2 = new HistoryResponse("test-user", timestamp, "test-data2");

        when(historyRepository.findAll(any(Specification.class)))
                .thenAnswer(invocation -> {
                    // Simulate filtering by manually returning filtered list
                    return Stream.of(history, history2)
                            .filter(h -> h.getCreatedAt().equals(timestamp))
                            .collect(Collectors.toList());
                });
        when(historyMapper.toResponse(history))
                .thenReturn(historyResponse);
        when(historyMapper.toResponse(history2))
                .thenReturn(historyResponse2);

        // Act
        List<HistoryResponse> result = rTreeService.getHistory(
                Optional.empty(),
                Optional.of(timestamp),
                Optional.empty()
        );

        // Assert
        assertEquals(2, result.size());
        assertEquals("test-user", result.getFirst().username());
        assertEquals(timestamp, result.getFirst().createdAt());
        assertEquals("test-data", result.getFirst().jsonData());
        assertEquals("test-user", result.get(1).username());
        assertEquals(timestamp, result.get(1).createdAt());
        assertEquals("test-data2", result.get(1).jsonData());
        verify(historyRepository).findAll(any(Specification.class));
        verify(historyMapper, times(2)).toResponse(any(History.class));
    }

    @Test
    @SuppressWarnings("unchecked") // We know that the mock is of type JpaSpecificationExecutor<History>
    void getHistory_WithUniqueDataSet_ReturnsOneResult() {
        // Arrange
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        History history = new History("test-user", timestamp, "test-data");
        History history2 = new History("test-user", timestamp, "test-data2");
        HistoryResponse historyResponse = new HistoryResponse("test-user", timestamp, "test-data");

        when(historyRepository.findAll(any(Specification.class)))
                .thenAnswer(invocation -> {
                    // Simulate filtering by manually returning filtered list
                    return Stream.of(history, history2)
                            .filter(h -> h.getJsonData().equals("test-data"))
                            .collect(Collectors.toList());
                });
        when(historyMapper.toResponse(history))
                .thenReturn(historyResponse);

        // Act
        List<HistoryResponse> result = rTreeService.getHistory(
                Optional.empty(),
                Optional.empty(),
                Optional.of("test-data")
        );

        // Assert
        assertEquals(1, result.size());
        assertEquals("test-user", result.getFirst().username());
        assertEquals(timestamp, result.getFirst().createdAt());
        assertEquals("test-data", result.getFirst().jsonData());
        verify(historyRepository).findAll(any(Specification.class));
        verify(historyMapper, times(1)).toResponse(any(History.class));
    }

    @Test
    @SuppressWarnings("unchecked") // We know that the mock is of type JpaSpecificationExecutor<History>
    void getHistory_WithNoParameters_ReturnsAllResults() {
        // Arrange
        History history = new History("test-user", new Timestamp(System.currentTimeMillis()), "test-data");
        HistoryResponse historyResponse = new HistoryResponse("test-user", history.getCreatedAt(), "test-data");

        when(historyRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(history));
        when(historyMapper.toResponse(history))
                .thenReturn(historyResponse);

        // Act
        List<HistoryResponse> result = rTreeService.getHistory(
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );

        // Assert
        assertEquals(1, result.size());
        verify(historyRepository).findAll(any(Specification.class));
        verify(historyMapper).toResponse(any(History.class));
    }

    @Test
    void generateTempDirectoryStructure_ReturnsPath() throws IOException {
        // Act
        String result = rTreeService.generateDirectoryStructure();

        // Assert
        assertNotNull(result);
        assertTrue(Files.exists(Path.of(result)));
    }

    @Test
    void generateTempDirectoryStructure_WithExistingTempDir_ReturnsNew_RemovesOld() throws IOException {
        // Act
        String first = rTreeService.generateDirectoryStructure();
        String second = rTreeService.generateDirectoryStructure();

        // Assert
        assertNotNull(first);
        assertNotNull(second);
        assertTrue(Files.exists(Path.of(second)));
        assertFalse(Files.exists(Path.of(first)));
    }
}
