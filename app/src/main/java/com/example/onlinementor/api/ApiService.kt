package com.example.onlinementor.api

import com.example.onlinementor.data.SignupRequest
import com.example.onlinementor.data.SignupResponse
import com.example.onlinementor.data.SigninRequest
import com.example.onlinementor.data.SigninResponse
import com.example.onlinementor.data.StudentDetailsData
import com.example.onlinementor.data.StudentDetailsResponse
import com.example.onlinementor.data.MentorDetailsData
import com.example.onlinementor.data.MentorDetailsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("signup.php")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    @POST("signin.php")
    suspend fun signin(@Body request: SigninRequest): Response<SigninResponse>

    @POST("student_details.php")
    suspend fun submitStudentDetails(@Body studentDetails: StudentDetailsData): Response<StudentDetailsResponse>

    @POST("mentor_details_form.php")
    suspend fun submitMentorDetails(@Body mentorDetails: MentorDetailsData): Response<MentorDetailsResponse>
}
