package com.kumaran.attendance_system.service;

import com.kumaran.attendance_system.dto.*;
import com.kumaran.attendance_system.entity.*;
import com.kumaran.attendance_system.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HodService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyAssignmentRepository facultyAssignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public Student addStudent(AddStudentRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .name(request.getName()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.STUDENT).build();
        user = userRepository.save(user);

        Student student = Student.builder().user(user)
                .registerNumber(request.getRegisterNumber())
                .department(request.getDepartment())
                .section(request.getSection()).build();
        return studentRepository.save(student);
    }

    public Faculty addFaculty(AddFacultyRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .name(request.getName()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.FACULTY).build();
        user = userRepository.save(user);

        Faculty faculty = Faculty.builder().user(user)
                .department(request.getDepartment()).build();
        return facultyRepository.save(faculty);
    }

    public Subject createSubject(CreateSubjectRequest request) {
        if (subjectRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Subject code already exists");
        }
        Subject subject = Subject.builder()
                .name(request.getName()).code(request.getCode())
                .department(request.getDepartment()).build();
        return subjectRepository.save(subject);
    }

    public FacultyAssignment assignSubject(AssignSubjectRequest request) {
        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        FacultyAssignment assignment = FacultyAssignment.builder()
                .faculty(faculty).subject(subject).section(request.getSection()).build();
        return facultyAssignmentRepository.save(assignment);
    }

    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(s -> new StudentResponse(s.getId(), s.getUser().getName(), s.getUser().getEmail(),
                        s.getRegisterNumber(), s.getDepartment(), s.getSection()))
                .collect(Collectors.toList());
    }

    public List<FacultyResponse> getAllFaculty() {
        return facultyRepository.findAll().stream()
                .map(f -> new FacultyResponse(f.getId(), f.getUser().getName(), f.getUser().getEmail(), f.getDepartment()))
                .collect(Collectors.toList());
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public byte[] exportAttendanceExcel(Long subjectId, String section) throws IOException {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Student> students = studentRepository.findBySection(section);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Register Number");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Total Classes");
            header.createCell(3).setCellValue("Present");
            header.createCell(4).setCellValue("Attendance %");

            int rowNum = 1;
            for (Student student : students) {
                List<AttendanceRecord> records = attendanceRecordRepository
                        .findByStudentIdAndSubjectId(student.getId(), subjectId);

                long total = records.size();
                long present = records.stream()
                        .filter(r -> r.getStatus() == AttendanceRecord.Status.PRESENT)
                        .count();
                double percentage = total == 0 ? 0 : (present * 100.0 / total);

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getRegisterNumber());
                row.createCell(1).setCellValue(student.getUser().getName());
                row.createCell(2).setCellValue(total);
                row.createCell(3).setCellValue(present);
                row.createCell(4).setCellValue(String.format("%.1f%%", percentage));
            }

            for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}