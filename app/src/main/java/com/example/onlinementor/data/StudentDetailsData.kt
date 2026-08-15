package com.example.onlinementor.data

data class StudentDetailsData(
    val full_name: String,
    val reg_number: String,
    val mobile_number: String,
    val date_of_birth: String,
    val gender: String,
    val college_name: String,
    val branch: String,
    val current_year: String,
    val email: String
)

data class StudentDetailsResponse(
    val status: String,
    val message: String
)
