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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    String user;

    Timestamp createdAt;

    @Column(columnDefinition = "TEXT")
    private String jsonData;

    public History(String user, Timestamp createdAt, String jsonData) {
        this.user = user;
        this.createdAt = createdAt;
        this.jsonData = jsonData;
    }
}
