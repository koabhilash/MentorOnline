package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class StudentDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        // Retrieve email from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val email: String? = sharedPreferences.getString("email", null)

        // Check if email is null
        if (email == null) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
        } else {
            // Initialize dashboard buttons
            val btn_profile = findViewById<ImageButton>(R.id.btn_profile)
            val notificationButton = findViewById<ImageButton>(R.id.btn_notification)
            val sendRequestCard = findViewById<CardView>(R.id.sendRequestCard)
            val checkStatusCard = findViewById<CardView>(R.id.checkStatusCard)
            val reportIssuesCard = findViewById<CardView>(R.id.reportIssuesCard)

            btn_profile.setOnClickListener {
                startActivity(Intent(this, StudentProfilePageActivity::class.java))
            }
            notificationButton.setOnClickListener {
                startActivity(Intent(this, StdNotificationActivity::class.java))
            }
            sendRequestCard.setOnClickListener {
                startActivity(Intent(this, SendRequestActivity::class.java))
            }
            checkStatusCard.setOnClickListener {
                startActivity(Intent(this, CheckStatusActivity::class.java))
            }
            reportIssuesCard.setOnClickListener {
                startActivity(Intent(this, ActivityReportIssue::class.java))
            }

            // Initialize navigation buttons
            val homeButton = findViewById<ImageView>(R.id.homeButton)
            val achButton = findViewById<ImageView>(R.id.achButton)
            val midButton = findViewById<ImageView>(R.id.midButton)
            val cerButton = findViewById<ImageView>(R.id.cerButton)
            val proButton = findViewById<ImageView>(R.id.proButton)

            homeButton.setOnClickListener {
                startActivity(Intent(this, CGPAActivity::class.java))
            }
            achButton.setOnClickListener {
                startActivity(Intent(this, AchievementAddActivity::class.java))
            }
            midButton.setOnClickListener {
                startActivity(Intent(this, StudentDashboardActivity::class.java))
            }
            cerButton.setOnClickListener {
                startActivity(Intent(this, CertificateAddActivity::class.java))
            }
            proButton.setOnClickListener {
                startActivity(Intent(this, StudentProfileActivity::class.java))
            }
        }
    }
}
