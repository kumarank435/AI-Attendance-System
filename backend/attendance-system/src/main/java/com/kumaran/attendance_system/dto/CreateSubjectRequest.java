package com.kumaran.attendance_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSubjectRequest {
    @NotBlank private String name;
    @NotBlank private String code;
    @NotBlank private String department;
}