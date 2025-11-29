package com.sid.rickmorty.repository;

import com.sid.rickmorty.model.entity.CharacterEntity;
import org.springframework.data.repository.CrudRepository;

public interface CharacterRepository extends CrudRepository<CharacterEntity, Integer> {
}
