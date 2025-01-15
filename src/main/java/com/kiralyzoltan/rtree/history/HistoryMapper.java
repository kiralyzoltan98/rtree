package com.kiralyzoltan.rtree.history;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryMapper {
    HistoryResponse toResponse(History history);
}
