package com.kiralyzoltan.rtree.history;

import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;

public class HistorySpecifications {
    public static Specification<History> hasUser(String user) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user"), user);
    }

    public static Specification<History> hasCreatedAt(Timestamp createdAt) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("createdAt"), createdAt);
    }

    public static Specification<History> hasJsonData(String jsonData) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("jsonData"), jsonData);
    }
}
