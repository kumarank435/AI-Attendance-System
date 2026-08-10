package com.kumaran.attendance_system.repository;

import com.kumaran.attendance_system.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findByStudentIdAndSubjectIdAndAttendanceDate(
            Long studentId, Long subjectId, LocalDate date);

    List<AttendanceRecord> findBySubjectIdAndSectionAndAttendanceDate(
            Long subjectId, String section, LocalDate date);

    List<AttendanceRecord> findByStudentId(Long studentId);

    List<AttendanceRecord> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
}