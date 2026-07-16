package com.specialed.assistant.mapper;

import com.specialed.assistant.entity.ObservationSessionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ObservationSessionMapper {
    int insert(ObservationSessionEntity entity);
    ObservationSessionEntity findById(Long id);
    int update(ObservationSessionEntity entity);
}
