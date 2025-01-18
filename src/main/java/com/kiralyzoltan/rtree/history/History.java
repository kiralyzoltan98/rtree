package com.kiralyzoltan.rtree.history;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    String username;

    Timestamp createdAt;

    @Column(columnDefinition = "TEXT")
    private String jsonData;

    public History(String username, Timestamp createdAt, String jsonData) {
        this.username = username;
        this.createdAt = createdAt;
        this.jsonData = jsonData;
    }
}
