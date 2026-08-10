package com.kumaran.attendance_system.repository;

import com.kumaran.attendance_system.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRegisterNumber(String registerNumber);
    Optional<Student> findByUserId(Long userId);
    List<Student> findBySection(String section);
    List<Student> findByUserName(String name);
}