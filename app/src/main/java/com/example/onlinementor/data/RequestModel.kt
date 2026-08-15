package com.example.onlinementor.data

data class RequestModel(
    val studentName: String,
    val regNumber: String,
    val reason: String,
    val taggingName: String,
    val taggingId: String,
    val startDate: String,
    val endDate: String,
    val proofs: String,
    val permissionLetter: String,
    val subject: String   // Default value added to prevent errors
)