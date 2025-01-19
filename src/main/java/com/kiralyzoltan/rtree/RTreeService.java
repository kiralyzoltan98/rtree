package com.kiralyzoltan.rtree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiralyzoltan.rtree.config.AppInstanceConfig;
import com.kiralyzoltan.rtree.history.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RTreeService {
    private static Path previousTempDir = null;

    private final AppInstanceConfig appInstanceConfig;
    private final HistoryRepository historyRepository;
    private final HistoryMapper historyMapper;

    public RTreeService(AppInstanceConfig appInstanceConfig, HistoryRepository historyRepository, HistoryMapper historyMapper) {
        this.appInstanceConfig = appInstanceConfig;
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;
    }

    public List<String> getUniqueFilenamesAndSaveHistory(String path, Optional<String> extension) throws IOException {
        List<String> uniqueFilenames = getUniqueFilenames(path, new HashMap<>(), extension);

        ObjectMapper mapper = new ObjectMapper();
        historyRepository.save(new History(appInstanceConfig.getInstanceName(), new Timestamp(System.currentTimeMillis()), mapper.writeValueAsString(uniqueFilenames)));

        return uniqueFilenames;
    }

    public List<String> getUniqueFilenames(String path, HashMap<String, Integer> filenames, Optional<String> extension) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path))) {
            for (Path item : stream) {
                String filename = item.getFileName().toString();
                if (Files.isDirectory(item)) {
                    getUniqueFilenames(item.toString(), filenames, extension);
                } else {
                    if (extension.isEmpty() || filename.endsWith(extension.get())) {
                        filenames.put(filename, filenames.getOrDefault(filename, 0) + 1);
                    }
                }
            }
        }

        return filenames.keySet().stream().filter(k -> filenames.get(k) == 1).toList();
    }

    public List<HistoryResponse> getHistory(Optional<String> username, Optional<Timestamp> createdAt, Optional<String> jsonData) {
        Specification<History> spec = Specification.where(null);

        if (username.isPresent()) {
            spec = spec.and(HistorySpecifications.hasUsername(username.get()));
        }
        if (createdAt.isPresent()) {
            spec = spec.and(HistorySpecifications.hasCreatedAt(createdAt.get()));
        }
        if (jsonData.isPresent()) {
            // This is a quick workaround to enable users to reuse the json data they got from the response
            String unescapedJsonData = jsonData.get().replace("\\", "");
            spec = spec.and(HistorySpecifications.hasJsonData(unescapedJsonData));
        }

        return historyRepository.findAll(spec).stream()
                .map(historyMapper::toResponse)
                .collect(Collectors.toList());
    }

    public String generateDirectoryStructure() throws IOException {
        if (previousTempDir != null && Files.exists(previousTempDir)) {
            FileSystemUtils.deleteRecursively(previousTempDir);
        }

        Path tempDir = Files.createTempDirectory("rtree");
        previousTempDir = tempDir;

        Path subDir = Files.createDirectory(tempDir.resolve("a"));
        Path subSubDir = Files.createDirectory(subDir.resolve("aa"));
        Path subSubSubDir = Files.createDirectory(subSubDir.resolve("aaa"));
        Path subDir2 = Files.createDirectory(tempDir.resolve("b"));
        Path subDir3 = Files.createDirectory(tempDir.resolve("c"));

        Files.createFile(tempDir.resolve("file1.txt")); // unique
        Files.createFile(tempDir.resolve("file2.txt"));
        Files.createFile(tempDir.resolve("file2.json")); // unique
        Files.createFile(subDir.resolve("file2.txt"));
        Files.createFile(subDir.resolve("file3.txt"));
        Files.createFile(subDir.resolve("file3.yaml"));
        Files.createFile(subSubDir.resolve("file3.txt"));
        Files.createFile(subSubDir.resolve("file3.yaml"));
        Files.createFile(subSubDir.resolve("file4.txt"));
        Files.createFile(subSubDir.resolve("file4.yaml")); // unique
        Files.createFile(subSubSubDir.resolve("file4.txt"));
        Files.createFile(subSubSubDir.resolve("file4.c")); // unique
        Files.createFile(subSubSubDir.resolve("file5.txt"));
        Files.createFile(subSubSubDir.resolve("file55.txt")); // unique
        Files.createFile(subDir2.resolve("file5.txt"));
        Files.createFile(subDir2.resolve("file6.txt"));
        Files.createFile(subDir2.resolve("file6.c")); // unique
        Files.createFile(subDir3.resolve("file6.txt"));
        Files.createFile(subDir3.resolve("file7.txt")); // unique

        return tempDir.toString();
    }
}
