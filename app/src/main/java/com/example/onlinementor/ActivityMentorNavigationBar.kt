package com.example.onlinementor

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ActivityMentorNavigationBar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_bar_mentor) // Replace with your actual layout file

        // Initialize buttons
        val homeButton = findViewById<ImageView>(R.id.homeButton)
        val achButton = findViewById<ImageView>(R.id.achButton)
        val midButton = findViewById<ImageView>(R.id.midButton)
        val cerButton = findViewById<ImageView>(R.id.cerButton)
        val proButton = findViewById<ImageView>(R.id.proButton)

        // Set click listeners for navigation
        homeButton.setOnClickListener {
            val intent = Intent(this, CGPAActivity::class.java)
            startActivity(intent)

        }

        achButton.setOnClickListener {
            val intent = Intent(this, AchievementAddActivity::class.java)
            startActivity(intent)

        }

        midButton.setOnClickListener {
            val intent = Intent(this, StudentDashboardActivity::class.java)
            startActivity(intent)
        }

        cerButton.setOnClickListener {
            val intent = Intent(this, CertificateAddActivity::class.java)
            startActivity(intent)

        }

        proButton.setOnClickListener {
            val intent = Intent(this, MentorPofileDetailsActivity::class.java)
            startActivity(intent)

        }
    }
}
