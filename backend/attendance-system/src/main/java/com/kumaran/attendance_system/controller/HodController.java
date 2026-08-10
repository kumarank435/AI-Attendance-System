package com.kumaran.attendance_system.controller;

import com.kumaran.attendance_system.dto.*;
import com.kumaran.attendance_system.entity.*;
import com.kumaran.attendance_system.service.HodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/hod")
@RequiredArgsConstructor
public class HodController {

    private final HodService hodService;

    @PostMapping("/students")
    public ResponseEntity<Student> addStudent(@Valid @RequestBody AddStudentRequest request) {
        return ResponseEntity.ok(hodService.addStudent(request));
    }

    @PostMapping("/faculty")
    public ResponseEntity<Faculty> addFaculty(@Valid @RequestBody AddFacultyRequest request) {
        return ResponseEntity.ok(hodService.addFaculty(request));
    }

    @PostMapping("/subjects")
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        return ResponseEntity.ok(hodService.createSubject(request));
    }

    @PostMapping("/assignments")
    public ResponseEntity<FacultyAssignment> assignSubject(@Valid @RequestBody AssignSubjectRequest request) {
        return ResponseEntity.ok(hodService.assignSubject(request));
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(hodService.getAllStudents());
    }

    @GetMapping("/faculty")
    public ResponseEntity<List<FacultyResponse>> getAllFaculty() {
        return ResponseEntity.ok(hodService.getAllFaculty());
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(hodService.getAllSubjects());
    }

    @GetMapping("/reports/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam Long subjectId, @RequestParam String section) throws IOException {
        byte[] excel = hodService.exportAttendanceExcel(subjectId, section);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}