package com.sid.rickmorty.repository;

import com.sid.rickmorty.model.entity.CharacterNoteEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CharacterNoteRepository extends CrudRepository<CharacterNoteEntity, Long> {

    @Query("SELECT * FROM character_notes WHERE character_id = :characterId ORDER BY created_at DESC")
    List<CharacterNoteEntity> findByCharacterId(int characterId);
}
