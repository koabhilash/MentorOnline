package com.example.onlinementor.models

data class LeaveRequest(
    val subjectToPermission: String,
    val taggingName: String,
    val startDate: String,
    val mentorStatus: String
)
