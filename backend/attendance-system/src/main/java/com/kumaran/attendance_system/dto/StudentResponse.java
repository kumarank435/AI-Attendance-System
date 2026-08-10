package com.kumaran.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String name;
    private String email;
    private String registerNumber;
    private String department;
    private String section;
}