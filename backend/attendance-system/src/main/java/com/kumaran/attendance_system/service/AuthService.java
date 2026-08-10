package com.kumaran.attendance_system.service;

import com.kumaran.attendance_system.dto.*;
import com.kumaran.attendance_system.entity.*;
import com.kumaran.attendance_system.repository.*;
import com.kumaran.attendance_system.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = userRepository.save(user);

        if (request.getRole() == User.Role.STUDENT) {
            if (request.getRegisterNumber() == null || request.getSection() == null || request.getDepartment() == null) {
                throw new RuntimeException("registerNumber, department and section are required for students");
            }
            Student student = Student.builder()
                    .user(user)
                    .registerNumber(request.getRegisterNumber())
                    .department(request.getDepartment())
                    .section(request.getSection())
                    .build();
            studentRepository.save(student);
        } else if (request.getRole() == User.Role.FACULTY) {
            if (request.getDepartment() == null) {
                throw new RuntimeException("department is required for faculty");
            }
            Faculty faculty = Faculty.builder()
                    .user(user)
                    .department(request.getDepartment())
                    .build();
            facultyRepository.save(faculty);
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}