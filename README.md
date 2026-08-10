# AI-Based Smart Classroom Attendance System

A full-stack attendance system that uses real-time face recognition to automatically mark student attendance from a single classroom photo — detecting and identifying multiple students simultaneously.

## Architecture

```
ai-attendance-system/
├── backend/                    → Spring Boot REST API (Java, MySQL, JWT auth)
├── face-recognition-service/   → Python microservice (OpenCV + face_recognition)
└── frontend/                   → HTML/CSS/JS single-page app (HOD, Faculty, Student portals)
```

The Spring Boot backend handles authentication, roles, business rules, and data storage. It delegates the actual face detection/recognition work to a separate Python microservice over HTTP — a genuine microservices pattern, since Python's computer vision ecosystem is far more mature than Java's for this task.

## Features

- **Role-based auth** (JWT) — HOD, Faculty, Student, each with restricted API access
- **HOD portal** — manage students, faculty, subjects, and faculty-subject-section assignments
- **Face enrollment** — register a student's face against their profile
- **Classroom scan** — faculty uploads one photo of the classroom; the system detects every visible face, matches each against the section's roster, and marks Present/Absent for the whole class in one request
- **Duplicate-safe attendance** — one record per student per subject per day, enforced at both the application and database level
- **Excel export** — attendance percentage reports per subject/section (Apache POI)
- **Student portal** — view personal attendance history and status

## Tech Stack

**Backend:** Java 21, Spring Boot 4.1, Spring Security + JWT, Spring Data JPA, MySQL, Apache POI, Maven
**AI Microservice:** Python, Flask, OpenCV, face_recognition (dlib)
**Frontend:** HTML, CSS, vanilla JavaScript

## How Recognition Works

1. Each student's face is enrolled under a unique label (`student_<id>`) via the Python service's `/enroll` endpoint — this avoids name collisions between students.
2. When faculty scans a classroom photo, the image is sent to Python's `/recognize` endpoint, which detects **every face in the frame** and returns a match (or "unknown") for each.
3. Spring Boot cross-references recognized labels against the section's full roster: matched students are marked **Present**, everyone else on the roster is marked **Absent** — mirroring how a real roll call works.

## API Overview

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` / `/login` | Public |
| POST | `/api/hod/students` / `/faculty` / `/subjects` / `/assignments` | HOD only |
| GET | `/api/hod/students` / `/faculty` / `/subjects` | HOD only |
| GET | `/api/hod/reports/excel` | HOD only |
| POST | `/api/students/{id}/enroll-face` | HOD / Faculty |
| POST | `/api/attendance/scan` | Faculty / HOD |
| GET | `/api/students/{id}/attendance` | Authenticated |

## Running Locally

**1. Python face recognition service**
```bash
cd face-recognition-service
python -m venv venv
.\venv\Scripts\Activate.ps1
pip install -r requirements.txt
python app.py
```
Runs on `http://localhost:5000`.

**2. Spring Boot backend**
```bash
cd backend/attendance-system
./mvnw spring-boot:run
```
Requires a local MySQL database (`attendance_db`) and config in `application.properties`. Runs on `http://localhost:8082`.

**3. Frontend**

Open `frontend/index.html` directly in a browser, or serve it with a simple local server (e.g. VS Code Live Server).

## Roles

| Role | Capabilities |
|---|---|
| HOD | Add students/faculty, create subjects, assign faculty to subject+section, view all data, export Excel reports |
| Faculty | Scan classroom attendance for their assigned subject/section |
| Student | View their own attendance history |

## Notable Engineering Challenges

- Resolved a chain of Windows-specific Python packaging issues: `dlib` failing to compile from source (worked around with a precompiled `dlib-bin` wheel), and a `pkg_resources`/`setuptools` deprecation breaking `face_recognition` (fixed by pinning `setuptools<81`).
- Designed a naming scheme (`student_<id>`) to bridge Python's name-based face labels with the relational database's primary keys, avoiding collisions between students with the same name.
- Enforced attendance uniqueness at the database level via a composite unique constraint on `(student_id, subject_id, attendance_date)`, in addition to application-level checks.
- Implemented role-based API access control (HOD/Faculty/Student) using Spring Security authorities derived from JWT claims.
