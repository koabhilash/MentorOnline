package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class CheckStatusActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var userEmail: String? = null  // Holds the correct email
    private var isMentor: Boolean = false  // Flag to check if it's a mentor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_status)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        // Retrieve email: Intent is ONLY for mentors, SharedPreferences for students
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val sharedPrefEmail = sharedPreferences.getString("email", null)
        val intentEmail = intent.getStringExtra("student_email")  // Used only by mentors

        // Determine user type
        if (intentEmail != null) {
            userEmail = intentEmail
            isMentor = true  // Mentor is viewing a student's status
        } else {
            userEmail = sharedPrefEmail
            isMentor = false // Regular student view
        }

        // Debugging Toasts


        // If no email is found, show an error and exit
        if (userEmail == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize views

        val certificatesCard = findViewById<CardView>(R.id.certificatesCard)
        val cgpaCard = findViewById<CardView>(R.id.cgpaCard)
        val achievementsCard = findViewById<CardView>(R.id.achievementsCard)



        certificatesCard.setOnClickListener {
            val intent = Intent(this, ActivityCertificate::class.java)
            intent.putExtra("email", userEmail) // Pass correct email
            startActivity(intent)
        }

        cgpaCard.setOnClickListener {
            val intent = Intent(this, CGPDisplayAActivity::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
        }

        achievementsCard.setOnClickListener {
            val intent = Intent(this, ActivityAchievements::class.java)
            intent.putExtra("email", userEmail)
            startActivity(intent)
        }
    }
}
