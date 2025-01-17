package com.kiralyzoltan.rtree.history;

import java.sql.Timestamp;

public record HistoryResponse(String username, Timestamp createdAt, String jsonData) {
}
