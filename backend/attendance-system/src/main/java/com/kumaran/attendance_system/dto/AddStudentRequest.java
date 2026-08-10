package com.kumaran.attendance_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddStudentRequest {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotBlank private String password;
    @NotBlank private String registerNumber;
    @NotBlank private String department;
    @NotBlank private String section;
}