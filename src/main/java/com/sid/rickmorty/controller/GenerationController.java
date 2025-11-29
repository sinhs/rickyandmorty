package com.sid.rickmorty.controller;

import com.sid.rickmorty.model.dto.GenerationRequest;
import com.sid.rickmorty.model.dto.GenerationResponse;
import com.sid.rickmorty.service.CharacterService;
import com.sid.rickmorty.service.GenerationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generations")
public class GenerationController {

    private final GenerationService generationService;
    private final CharacterService characterService;

    public GenerationController(GenerationService generationService, CharacterService characterService) {
        this.generationService = generationService;
        this.characterService = characterService;
    }

    @PostMapping
    public GenerationResponse generate(@Valid @RequestBody GenerationRequest request) {
        String context = characterService.getCharacter(request.subjectId())
                .map(dto -> dto.name() + " is a " + dto.species() + " from " + dto.origin())
                .orElse("Unknown subject");
        return generationService.generate(request, context);
    }
}
