package com.kumaran.attendance_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faculty_assignments", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"faculty_id", "subject_id", "section"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private String section;
}