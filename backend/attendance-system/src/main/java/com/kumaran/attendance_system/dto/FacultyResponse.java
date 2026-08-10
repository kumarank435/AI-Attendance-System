package com.kumaran.attendance_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class FacultyResponse {
    private Long id;
    private String name;
    private String email;
    private String department;
}