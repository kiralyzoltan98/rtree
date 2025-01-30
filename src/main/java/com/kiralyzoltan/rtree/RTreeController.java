package com.kiralyzoltan.rtree;

import com.kiralyzoltan.rtree.history.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @Operation(summary = "Get unique filenames in a directory and its subdirectories")
    @GetMapping("/getunique")
    public List<String> getUniqueFilenames(@RequestParam @Parameter(description = "Absolute path to find unique files in") String path,
                                           @RequestParam @Parameter(description = "extension filter, example values: \"txt\" \"json\" \"yaml\" \"c\" or multiple separated by commas: \"txt,json\"")
                                           Optional<String> extension)
        throws IOException {
        return rTreeService.getUniqueFilenamesAndSaveHistory(path, extension);
    }

    @Operation(summary = "Get history of the /getunique endpoint's requests. (who, when, what) \n Without parameters it returns all history entries.")
    @GetMapping("/history")
    public List<HistoryResponse> getHistory(@RequestParam @Parameter(description = "Use the instance name of the application here (Instance1, Instance2)")
                                            Optional<String> username,
                                            @RequestParam Optional<Timestamp> createdAt,
                                            @RequestParam @Parameter(description = "You can reuse a previous /history's jsonData here, it handles the escaped json that it returns.")
                                                Optional<String> jsonData) { // The json data would fit better in the request body
        return rTreeService.getHistory(username, createdAt, jsonData);
    }

    @Operation(summary = "Generate a directory structure with files and subdirectories to test the /getunique endpoint")
    @PutMapping("/generate")
    public String generate() throws IOException {
        return rTreeService.generateDirectoryStructure();
    }
}
