package com.kiralyzoltan.rtree.history;

import java.sql.Timestamp;

public record HistoryResponse(String user, Timestamp createdAt, String jsonData) {
}
