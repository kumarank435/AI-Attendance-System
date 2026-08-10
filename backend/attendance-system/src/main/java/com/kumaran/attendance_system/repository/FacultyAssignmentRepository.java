package com.kumaran.attendance_system.repository;

import com.kumaran.attendance_system.entity.FacultyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacultyAssignmentRepository extends JpaRepository<FacultyAssignment, Long> {
    List<FacultyAssignment> findByFacultyId(Long facultyId);
}