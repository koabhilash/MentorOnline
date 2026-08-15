package com.example.onlinementor.data

data class MentorDetailsData(
    val email: String,
    val full_name: String,
    val mentor_id: String,
    val mobile_number: String,
    val date_of_birth: String,
    val gender: String,
    val college: String,
    val department: String
)
data class MentorDetailsResponse(
    val status: String,
    val message: String
)