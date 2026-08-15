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



class MentorDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentor_dashboard)



        // Retrieve email from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val email: String? = sharedPreferences.getString("email", null)

        // Check if email is null and handle appropriately
        if (email == null) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
        } else {
            // Initialize views
            val btnProfile = findViewById<ImageButton>(R.id.btn_profile)
            val notificationButton = findViewById<ImageButton>(R.id.btn_notification)
            val requestsCard = findViewById<CardView>(R.id.requestsCard)
            val trackStatusCard = findViewById<CardView>(R.id.trackStatusCard)
            val resolveIssuesCard = findViewById<CardView>(R.id.resolveIssuesCard)

            // Set up click listeners
            btnProfile.setOnClickListener {
                val intent = Intent(this, MentorAccount::class.java)
                startActivity(intent)
            }

            notificationButton.setOnClickListener {

                val intent = Intent(this, TaggingRequestActivity::class.java)
                startActivity(intent)
            }

            requestsCard.setOnClickListener {
                val intent = Intent(this, RequestsActivity::class.java)
                startActivity(intent)

            }

            trackStatusCard.setOnClickListener {
                val intent = Intent(this, SearchStudentActivity::class.java)
                startActivity(intent)
            }

            resolveIssuesCard.setOnClickListener {

                val intent = Intent(this, IssuesActivity::class.java)
                startActivity(intent)
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
                startActivity(Intent(this, MentorActivity::class.java))
            }
            midButton.setOnClickListener {
                startActivity(Intent(this, MentorDashboardActivity::class.java))
            }
            cerButton.setOnClickListener {
                startActivity(Intent(this, DisplayMenteesActivity::class.java))
            }
            proButton.setOnClickListener {
                startActivity(Intent(this, MentorPofileDetailsActivity::class.java))
            }

        }
    }
}
