# UTD Attendance System: JavaFX App

## Introduction
This JavaFX app helps track student attendance at UTD. It loads student rosters and attendance data from CSV files. It then shows both a numerical and visual representation of attendance statistics.

## Software
- Java 11 via OpenJDK
- JavaFX via OpenJFX

## How to Compile and Run on Fedora Linux
- Compile: `javac --module-path /usr/lib/jvm/openjfx --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web AttendanceApp.java`
- Run: `java --module-path /usr/lib/jvm/openjfx --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web AttendanceApp`

## Usage
First click 'File' and load the student roster.
Then click 'File' and load the attendance.

Student Roster contains all students.
Attendance contains the students who were present.
