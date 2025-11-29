package com.sid.rickmorty.controller;

import com.sid.rickmorty.model.dto.CharacterDetailDto;
import com.sid.rickmorty.model.dto.CharacterNoteDto;
import com.sid.rickmorty.model.dto.NoteRequest;
import com.sid.rickmorty.service.CharacterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CharacterDetailDto> get(@PathVariable int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Optional<CharacterDetailDto> character = characterService.getCharacter(id);
        return character.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<CharacterNoteDto> addNote(@PathVariable int id, @Valid @RequestBody NoteRequest request) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        if (characterService.getCharacter(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(characterService.addNote(id, request));
    }
}
