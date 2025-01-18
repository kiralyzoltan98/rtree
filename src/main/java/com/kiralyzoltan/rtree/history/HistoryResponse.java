package com.kiralyzoltan.rtree.history;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public record HistoryResponse(String username,
                              @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS000", timezone = "Europe/Budapest")
                              Timestamp createdAt,
                              String jsonData) {
}
