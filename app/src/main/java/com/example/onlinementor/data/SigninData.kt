package com.example.onlinementor.data

data class SigninRequest(
    val email: String,
    val password: String
)

data class SigninResponse(
    val status: String,
    val message: String
)
