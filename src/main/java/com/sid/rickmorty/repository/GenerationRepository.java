package com.sid.rickmorty.repository;

import com.sid.rickmorty.model.entity.GenerationEntity;
import org.springframework.data.repository.CrudRepository;

public interface GenerationRepository extends CrudRepository<GenerationEntity, Long> {
}
