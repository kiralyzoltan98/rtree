package com.kiralyzoltan.rtree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiralyzoltan.rtree.config.AppInstanceConfig;
import com.kiralyzoltan.rtree.history.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RTreeService {

    private final AppInstanceConfig appInstanceConfig;
    private final HistoryRepository historyRepository;
    private final HistoryMapper historyMapper;

    public RTreeService(AppInstanceConfig appInstanceConfig, HistoryRepository historyRepository, HistoryMapper historyMapper) {
        this.appInstanceConfig = appInstanceConfig;
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;
    }

    @GetMapping("/getunique")
    public List<String> getUniqueFilenames(@RequestParam String path, @RequestParam Optional<String> extension) throws IOException { // TODO: implement extension filtering
        HashMap<String, Integer> filenames = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path))) {
            for (Path file : stream) {
                String filename = file.getFileName().toString();
                filenames.put(filename, filenames.getOrDefault(filename, 0) + 1);
            }
        }

        List<String> uniqueFilenames = filenames.keySet().stream().filter(k -> filenames.get(k) == 1).toList();

        ObjectMapper mapper = new ObjectMapper();
        historyRepository.save(new History(appInstanceConfig.getInstanceName(), new Timestamp(System.currentTimeMillis()), mapper.writeValueAsString(uniqueFilenames)));


        return uniqueFilenames;
    }

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
            // This is a quick workaround to enable users to reuse the json data they got from the response
            String unescapedJsonData = jsonData.get().replace("\\", "");
            spec = spec.and(HistorySpecifications.hasJsonData(unescapedJsonData));
        }

        return historyRepository.findAll(spec).stream()
                .map(historyMapper::toResponse)
                .collect(Collectors.toList());
    }
}
