package com.example.onlinementor.data

// Individual student details
data class Student(
    val full_name: String,
    val reg_number: String,
    val email: String
)

// Response model for student search results
data class StudentSearchResponse(
    val status: String,
    val students: List<Student>
)
