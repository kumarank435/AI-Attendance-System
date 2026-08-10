package com.kumaran.attendance_system.controller;

import com.kumaran.attendance_system.dto.AttendanceRecordResponse;
import com.kumaran.attendance_system.entity.Student;
import com.kumaran.attendance_system.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/students/{studentId}/enroll-face")
    public ResponseEntity<Map<String, String>> enrollFace(
            @PathVariable Long studentId,
            @RequestParam("image") MultipartFile image) throws IOException {
        attendanceService.enrollStudentFace(studentId, image);
        return ResponseEntity.ok(Map.of("message", "Face enrolled successfully"));
    }

    @PostMapping("/attendance/scan")
    public ResponseEntity<Map<String, Object>> scanClassroom(
            @RequestParam("facultyId") Long facultyId,
            @RequestParam("subjectId") Long subjectId,
            @RequestParam("section") String section,
            @RequestParam("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok(attendanceService.scanClassroomAttendance(facultyId, subjectId, section, image));
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(attendanceService.getAllStudents());
    }

    @GetMapping("/students/{studentId}/attendance")
    public ResponseEntity<List<AttendanceRecordResponse>> getStudentAttendance(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getStudentAttendance(studentId));
    }

    @GetMapping("/attendance/section-today")
    public ResponseEntity<List<AttendanceRecordResponse>> getSectionAttendanceToday(
            @RequestParam Long subjectId, @RequestParam String section) {
        return ResponseEntity.ok(attendanceService.getSectionAttendanceToday(subjectId, section));
    }
}