package com.kumaran.attendance_system.service;

import com.kumaran.attendance_system.client.PythonServiceClient;
import com.kumaran.attendance_system.dto.AttendanceRecordResponse;
import com.kumaran.attendance_system.entity.*;
import com.kumaran.attendance_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyAssignmentRepository facultyAssignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final PythonServiceClient pythonServiceClient;

    public void enrollStudentFace(Long studentId, MultipartFile image) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Map<String, Object> pythonResponse = pythonServiceClient.enroll(
                "student_" + student.getId(), image);

        if (pythonResponse.containsKey("error")) {
            throw new RuntimeException("Face enrollment failed: " + pythonResponse.get("error"));
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> scanClassroomAttendance(Long facultyId, Long subjectId, String section, MultipartFile image) throws IOException {
        boolean isAssigned = facultyAssignmentRepository.findByFacultyId(facultyId).stream()
                .anyMatch(a -> a.getSubject().getId().equals(subjectId) && a.getSection().equals(section));

        if (!isAssigned) {
            throw new RuntimeException("Faculty is not assigned to this subject/section");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Student> rosterStudents = studentRepository.findBySection(section);

        if (rosterStudents.isEmpty()) {
            throw new RuntimeException("No students found in section " + section);
        }

        Map<String, Object> pythonResponse = pythonServiceClient.recognize(image);
        List<Map<String, Object>> faces = (List<Map<String, Object>>) pythonResponse.get("faces");

        Set<Long> recognizedStudentIds = new HashSet<>();

        if (faces != null) {
            for (Map<String, Object> face : faces) {
                Boolean recognized = (Boolean) face.get("recognized");
                if (recognized != null && recognized) {
                    String label = (String) face.get("name");
                    if (label != null && label.startsWith("student_")) {
                        try {
                            Long recognizedId = Long.parseLong(label.substring("student_".length()));
                            recognizedStudentIds.add(recognizedId);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        int presentCount = 0;
        int absentCount = 0;

        for (Student student : rosterStudents) {
            boolean alreadyMarked = attendanceRecordRepository
                    .findByStudentIdAndSubjectIdAndAttendanceDate(student.getId(), subjectId, today)
                    .isPresent();

            if (alreadyMarked) {
                continue;
            }

            AttendanceRecord.Status status = recognizedStudentIds.contains(student.getId())
                    ? AttendanceRecord.Status.PRESENT
                    : AttendanceRecord.Status.ABSENT;

            if (status == AttendanceRecord.Status.PRESENT) {
                presentCount++;
            } else {
                absentCount++;
            }

            AttendanceRecord record = AttendanceRecord.builder()
                    .student(student)
                    .subject(subject)
                    .section(section)
                    .attendanceDate(today)
                    .status(status)
                    .markedAt(now)
                    .build();

            attendanceRecordRepository.save(record);
        }

        return Map.of(
                "subject", subject.getName(),
                "section", section,
                "date", today.toString(),
                "totalStudents", rosterStudents.size(),
                "present", presentCount,
                "absent", absentCount,
                "message", "Attendance recorded for " + section
        );
    }

    public List<AttendanceRecordResponse> getStudentAttendance(Long studentId) {
        return attendanceRecordRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AttendanceRecordResponse> getSectionAttendanceToday(Long subjectId, String section) {
        return attendanceRecordRepository
                .findBySubjectIdAndSectionAndAttendanceDate(subjectId, section, LocalDate.now())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    private AttendanceRecordResponse toResponse(AttendanceRecord r) {
        return new AttendanceRecordResponse(
                r.getId(),
                r.getStudent().getId(),
                r.getStudent().getUser().getName(),
                r.getSubject().getId(),
                r.getSubject().getName(),
                r.getSection(),
                r.getAttendanceDate(),
                r.getStatus().name(),
                r.getMarkedAt(),
                r.getConfidence()
        );
    }
}