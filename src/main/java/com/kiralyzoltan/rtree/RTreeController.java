package com.kiralyzoltan.rtree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiralyzoltan.rtree.config.AppInstanceConfig;
import com.kiralyzoltan.rtree.history.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class RTreeController {

    private final HistoryRepository historyRepository;
    private final HistoryMapper historyMapper;
    private final AppInstanceConfig appInstanceConfig;

    public RTreeController(HistoryRepository historyRepository, HistoryMapper historyMapper, AppInstanceConfig appInstanceConfig) {
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;
        this.appInstanceConfig = appInstanceConfig;
    }

    @GetMapping("/getunique")
    public List<String> getUniqueFilenames(@RequestParam String path, @RequestParam Optional<String> extension) { // TODO: implement extension filtering
        HashMap<String, Integer> filenames = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path))) {
            for (Path file : stream) {
                String filename = file.getFileName().toString();
                filenames.put(filename, filenames.getOrDefault(filename, 0) + 1);
            }
        } catch (IOException | DirectoryIteratorException e) {
            log.error("Error could not read directory: {}", e.getMessage());
        }

        List<String> uniqueFilenames = filenames.keySet().stream().filter(k -> filenames.get(k) == 1).toList();

        try {
            ObjectMapper mapper = new ObjectMapper();
            //TODO: get the appinstance/appid from properties
            historyRepository.save(new History(appInstanceConfig.getInstanceName(), new Timestamp(System.currentTimeMillis()), mapper.writeValueAsString(uniqueFilenames)));
        } catch (JsonProcessingException e) {
            log.error("Error could not save history: {}", e.getMessage());
        }

        return uniqueFilenames;
    }

    @GetMapping("/history")
    public List<HistoryResponse> getHistory(@RequestParam Optional<String> username,
                                            @RequestParam Optional<Timestamp> createdAt,
                                            @RequestParam Optional<String> jsonData)
    {
        Specification<History> spec = Specification.where(null);

        if (username.isPresent()) {
            spec = spec.and(HistorySpecifications.hasUsername(username.get()));
        }
        if (createdAt.isPresent()) {
            spec = spec.and(HistorySpecifications.hasCreatedAt(createdAt.get()));
        }
        if (jsonData.isPresent()) {
            spec = spec.and(HistorySpecifications.hasJsonData(jsonData.get()));
        }
        //TODO: fix Timestamp format to be able to be reused from a response.
        return historyRepository.findAll(spec).stream()
                .map(historyMapper::toResponse)
                .collect(Collectors.toList());
    }
}
