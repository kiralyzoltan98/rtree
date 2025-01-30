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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     *
     * @param path An absolute path to a directory we want to search for unique filenames.
     * @param extension An optional file extension to filter the search by (e.g. "txt" or "json") you can list multiple extensions separated by commas. ("txt,json")
     * @return A list of unique filenames that only appear once in the directory and its subdirectories.
     * @throws IOException if the input path is not a directory or if the directory does not exist.
     * It can also throw the IOException if the user does not have the necessary permissions to read the directory.
     */
    public List<String> getUniqueFilenamesAndSaveHistory(String path, Optional<String> extension) throws IOException {
        List<String> extensions = new ArrayList<>();
        if (extension.isPresent() && extension.get().contains(",")) {
            extensions = Stream.of(extension.get().split(",")).map(String::trim).toList();
        } else if (extension.isPresent()) {
            extensions.add(extension.get());
        }

        List<String> uniqueFilenames = getUniqueFilenames(path, new HashMap<>(), Optional.of(extensions));

        ObjectMapper mapper = new ObjectMapper();
        historyRepository.save(new History(appInstanceConfig.getInstanceName(), new Timestamp(System.currentTimeMillis()), mapper.writeValueAsString(uniqueFilenames)));

        return uniqueFilenames;
    }

    /**
     * Recursive function to search for unique filenames in a directory and its subdirectories.
     * @param path The absolute path of the directory we want to search for unique filenames in.
     * @param filenames A map that stores the filenames and their occurrence count.
     * @param extensions An optional file extensions to filter the search by (e.g. ".txt" or ".json") only one at a time is supported for now.
     * @return A list of unique filenames that only appear once in the directory and its subdirectories.
     * @throws IOException if the input path is not a directory or if the directory does not exist.
     * It can also throw the IOException if the user does not have the necessary permissions to read the directory.
     */
    public List<String> getUniqueFilenames(String path, HashMap<String, Integer> filenames, Optional<List<String>> extensions) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path))) {
            for (Path item : stream) {
                String filename = item.getFileName().toString();
                if (Files.isDirectory(item)) {
                    getUniqueFilenames(item.toString(), filenames, extensions);
                } else {
                    if (extensions.isEmpty() || extensions.get().isEmpty() || extensions.get().stream().anyMatch(filename::endsWith)) {
                        filenames.put(filename, filenames.getOrDefault(filename, 0) + 1);
                    }
                }
            }
        }

        return filenames.keySet().stream().filter(k -> filenames.get(k) == 1).toList();
    }

    /**
     *
     * @param username Filters the result by the username column in the database.
     * @param createdAt Filters the result by the createdAt column in the database.
     * @param jsonData Filters the result by the jsonData column in the database.
     * @return A list of HistoryResponse objects that match the given filters.
     */
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

    /**
     *
     * @return The absolute path of the generated directory structure. Reusable at the /getunique endpoint.
     * @throws IOException if the directory cannot be created or if the user does not have the necessary permissions to create the directory.
     */
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
