# MentorOnline

MentorOnline is an Android application designed to streamline communication between students and mentors while managing academic activities, permissions, approvals, and student records in one platform.

## Features

- Student-Mentor Communication
- Digital Permission Requests
- Mentor Approval System
- **Mentor/Invigilator Tagging for Transparent Permission Verification**
- **Tag-Based Approval Tracking to Prevent Fake Permissions and Unauthorized Approvals**
- Student Academic Tracking
- CGPA and Attendance Tracking
- NPTEL Certificate Management
- Course Management
- Issue Reporting
- Role-Based Access Control
- Student Profile Management

### Transparent Permission & Approval System

The application uses a mentor/invigilator tagging mechanism to associate permission requests with the appropriate authorized personnel. This improves transparency, enables approval tracking, and helps prevent fake permissions or unauthorized approvals.

## Tech Stack

- **Frontend:** Android, XML
- **Programming Language:** Kotlin
- **Backend:** PHP
- **Database:** MySQL
- **Development Environment:** Android Studio, XAMPP
- **Version Control:** Git & GitHub

## Project Structure

MentorOnline/
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           ├── res/
│           └── AndroidManifest.xml
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
└── README.md

## Requirements

- Android 8.0 or higher
- Android Studio
- MySQL
- XAMPP
- PHP

## Installation

1. Clone the repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync and download the required dependencies.
4. Configure the PHP backend and MySQL database.
5. Update the API configuration if required.
6. Connect an Android device or start an emulator.
7. Build and run the application.

## Application Modules

### Student Module

- View academic information
- Track attendance and CGPA
- Manage certificates
- Submit permission requests
- Report issues
- Communicate with mentors

### Mentor Module

- Review student requests
- Approve or reject permissions
- Monitor student information
- Manage academic-related activities

## Backend Setup

The application uses a PHP and MySQL backend hosted locally using XAMPP.

The APK provided in the release is intended for demonstration purposes. 
Backend-dependent features require the PHP server and MySQL database to be configured and running.

The backend source code and database configuration are not included in the APK.

## Author

**K. Abhilash**

Computer Science & Engineering
