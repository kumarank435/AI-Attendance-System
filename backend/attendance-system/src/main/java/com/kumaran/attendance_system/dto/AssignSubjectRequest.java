package com.kumaran.attendance_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignSubjectRequest {
    @NotNull private Long facultyId;
    @NotNull private Long subjectId;
    @NotBlank private String section;
}