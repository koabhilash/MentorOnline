package com.example.onlinementor.data

data class LeaveRequestModel(
    val studentName: String,
    val regNumber: String,
    val subjectToPermission: String,
    val taggedPersonName: String,
    val taggingId: String,
    val startDate: String,
    val mentorStatus: String
)
