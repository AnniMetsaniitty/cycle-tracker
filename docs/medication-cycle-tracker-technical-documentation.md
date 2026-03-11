# Medication & Cycle Tracker

## Technical Documentation

Implementation status on March 11, 2026:

- Spring Boot REST backend is implemented in the repository
- H2/JPA persistence is implemented for development
- User, Cycle, and Medication domain models are implemented
- REST endpoints for login, cycle management, and medication status are implemented
- An initial JavaFX desktop client is implemented for login, registration, dashboard metrics, medication status, and cycle history
- Richer statistics, notifications, and broader UI coverage are still planned

------------------------------------------------------------------------

# 1. Project Overview

## Purpose

The **Medication & Cycle Tracker** is a Java-based application designed
to help users track menstrual cycles and hormone replacement therapy
schedules.

The system allows users to: - Manually start and end menstrual cycles -
Track current cycle day - Automatically determine medication periods -
View cycle history - Generate cycle statistics (future feature)

Unlike many cycle trackers that rely on predictions, this application is
designed around **user-controlled cycle management**, making it
medically realistic and technically reliable.

------------------------------------------------------------------------

# 2. System Architecture

The application follows a layered architecture separating
responsibilities between the UI, business logic, and data persistence.

Architecture Layers:

JavaFX GUI\
↓\
Controllers\
↓\
Services (Business Logic)\
↓\
Repositories (Data Access)\
↓\
Database

Benefits: - Clear separation of concerns - Easy to maintain - Scalable
for future features - Testable components

------------------------------------------------------------------------

# 3. Technology Stack

  Component       Technology
  --------------- -------------------------------
  Language        Java
  UI              JavaFX
  Backend         Spring Boot
  Database        PostgreSQL / H2 (development)
  ORM             Spring Data JPA
  Build Tool      Maven
  Documentation   Markdown + Mermaid

------------------------------------------------------------------------

# 4. Project Structure

Example structure:

src/main/java/com/cycletracker

model/ User.java Cycle.java Medication.java

repository/ UserRepository.java CycleRepository.java
MedicationRepository.java

service/ CycleService.java MedicationService.java UserService.java

controller/ CycleController.java UserController.java

ui/ MainApp.java controllers/ fxml/

resources/ application.properties styles.css

------------------------------------------------------------------------

# 5. Domain Model

## User

  Field      Type
  ---------- --------
  id         Long
  username   String
  password   String
  email      String

## Cycle

  Field       Type
  ----------- ----------------------
  id          Long
  user        User
  startDate   LocalDate
  endDate     LocalDate (nullable)

Rules: - A cycle is active if `endDate == null` - Only one active cycle
allowed per user

## Medication

  Field      Type
  ---------- ---------
  id         Long
  cycle      Cycle
  startDay   int
  endDay     int
  taken      boolean

Medication window:

Day 16 → Day 26

------------------------------------------------------------------------

# 6. Database Design

Entities:

USER\
CYCLE\
MEDICATION

Relationships:

User → Cycles\
Cycle → Medication

------------------------------------------------------------------------

# 7. Core Business Logic

## Start New Cycle

Rules:

1.  Check for active cycle
2.  If active cycle exists → end it
3.  Create new cycle

## End Current Cycle

1.  Check if cycle exists
2.  Set endDate = today
3.  Save cycle

## Calculate Current Cycle Day

Formula:

currentDay = Period.between(startDate, today).getDays() + 1

------------------------------------------------------------------------

# 8. Medication Logic

Medication is active if:

day \>= 16 && day \<= 26

System determines medication status dynamically based on the current
cycle day.

------------------------------------------------------------------------

# 9. API Endpoints

  Endpoint             Method   Description
  -------------------- -------- -----------------------
  /user/login          POST     Login user
  /cycle/start         POST     Start new cycle
  /cycle/end           POST     End current cycle
  /cycle/current       GET      Get active cycle
  /medication/status   GET      Get medication status

------------------------------------------------------------------------

# 10. User Interface

## Login Screen

Controls: - Username - Password - Login - Register

## Dashboard

Displays: - Current cycle day - Medication status - Next medication date

Buttons: - Start New Cycle - End Current Cycle - View History -
Medication Status

------------------------------------------------------------------------

# 11. Error Handling

  Scenario                Handling
  ----------------------- ----------------------------------
  No active cycle         Show message
  Duplicate cycle start   End previous cycle automatically
  Invalid login           Show error message

------------------------------------------------------------------------

# 12. Future Enhancements

Possible improvements:

Statistics: - Average cycle length - Longest cycle - Shortest cycle

Notifications: - Medication reminders - Cycle predictions

Mobile version: - Android - Progressive Web App

------------------------------------------------------------------------

# 13. Security Considerations

-   Password hashing
-   Input validation
-   Secure database access

------------------------------------------------------------------------

# 14. Testing Strategy

Unit Tests: - CycleService - MedicationService

Integration Tests: - Repository access - API endpoints

------------------------------------------------------------------------

# 15. Deployment

Run example:

java -jar cycle-tracker.jar

Possible deployment: - Local machine - Docker container - Cloud service

------------------------------------------------------------------------

# 16. Portfolio Value

This project demonstrates:

-   Domain-driven design
-   Java backend architecture
-   JavaFX UI development
-   Database modeling
-   Business rule implementation

It highlights real-world thinking and user-centered design.

------------------------------------------------------------------------

# 17. License

MIT License
