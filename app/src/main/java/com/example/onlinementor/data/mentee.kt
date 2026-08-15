package com.example.onlinementor

data class Mentee(
    val mentee_name: String,
    val mentee_reg_num: String,
    var isSelected: Boolean = false
)