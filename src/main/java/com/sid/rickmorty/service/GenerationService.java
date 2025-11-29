package com.sid.rickmorty.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sid.rickmorty.client.GenerativeClient;
import com.sid.rickmorty.evaluation.EvaluationResult;
import com.sid.rickmorty.evaluation.EvaluationService;
import com.sid.rickmorty.model.dto.GenerationRequest;
import com.sid.rickmorty.model.dto.GenerationResponse;
import com.sid.rickmorty.model.entity.GenerationEntity;
import com.sid.rickmorty.repository.GenerationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GenerationService {

    private static final Logger log = LoggerFactory.getLogger(GenerationService.class);

    private final GenerativeClient generativeClient;
    private final EvaluationService evaluationService;
    private final GenerationRepository generationRepository;
    private final ObjectMapper mapper;

    public GenerationService(GenerativeClient generativeClient,
                             EvaluationService evaluationService,
                             GenerationRepository generationRepository,
                             ObjectMapper mapper) {
        this.generativeClient = generativeClient;
        this.evaluationService = evaluationService;
        this.generationRepository = generationRepository;
        this.mapper = mapper;
    }

    public GenerationResponse generate(GenerationRequest request, String context) {
        String prompt = buildPrompt(request, context);
        String output = generativeClient.generateText(prompt, context);
        EvaluationResult evaluation = evaluationService.evaluate(context, output);
        
        try {
            persist(request, prompt, output, evaluation);
        } catch (Exception e) {
            log.warn("Failed to persist generation, continuing with response", e);
        }
        
        return new GenerationResponse(
                request.subjectId(),
                request.subjectType(),
                prompt,
                output,
                evaluation
        );
    }

    private String buildPrompt(GenerationRequest request, String context) {
        return String.format(
                "You are narrating in the Rick & Morty universe. %s\n\nContext: %s",
                request.instruction(),
                context
        );
    }

    private void persist(GenerationRequest request, String prompt, String output, EvaluationResult evaluation) 
            throws JsonProcessingException {
        String evaluationJson = mapper.writeValueAsString(evaluation);
        GenerationEntity entity = GenerationEntity.of(
                request.subjectType(),
                request.subjectId(),
                prompt,
                output,
                evaluationJson
        );
        generationRepository.save(entity);
    }
}
