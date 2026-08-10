package com.kumaran.attendance_system.dto;

import com.kumaran.attendance_system.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 6) private String password;
    @NotNull private User.Role role;

    private String registerNumber;
    private String department;
    private String section;
}