package com.kiralyzoltan.rtree;

import com.kiralyzoltan.rtree.history.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@RestController
public class RTreeController {

    private final RTreeService rTreeService;

    public RTreeController(RTreeService rTreeService) {
        this.rTreeService = rTreeService;
    }

    @GetMapping("/getunique")
    public List<String> getUniqueFilenames(@RequestParam String path, @RequestParam Optional<String> extension) throws IOException {
        return rTreeService.getUniqueFilenamesAndSaveHistory(path, extension);
    }

    @GetMapping("/history")
    public List<HistoryResponse> getHistory(@RequestParam Optional<String> username,
                                            @RequestParam Optional<Timestamp> createdAt,
                                            @RequestParam Optional<String> jsonData)    // The json data would fit better in the request body
    {
        return rTreeService.getHistory(username, createdAt, jsonData);
    }
}
