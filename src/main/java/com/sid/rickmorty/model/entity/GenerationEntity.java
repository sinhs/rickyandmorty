package com.sid.rickmorty.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("generations")
public record GenerationEntity(
        @Id Long id,
        String subjectType,
        Integer subjectId,
        String prompt,
        String output,
        String evaluator
) {
    public static GenerationEntity of(String subjectType, Integer subjectId, String prompt, String output, String evaluatorJson) {
        return new GenerationEntity(null, subjectType, subjectId, prompt, output, evaluatorJson);
    }
}
