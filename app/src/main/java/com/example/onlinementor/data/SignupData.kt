package com.example.onlinementor.data

data class SignupRequest(
    val email: String,
    val password: String,
    val confirm_password: String
)

data class SignupResponse(
    val status: String,
    val message: String
)
