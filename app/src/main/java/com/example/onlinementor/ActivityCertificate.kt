package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ActivityCertificate : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var userEmail: String? = null  // Holds the correct email
    private var isMentor: Boolean = false  // Flag to check if mentor is viewing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificates)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        // Retrieve SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val sharedPrefEmail = sharedPreferences.getString("email", null)
        val intentEmail = intent.getStringExtra("email")  // Used only by mentors

        // Determine user type:
        // - If Intent email exists → Mentor is viewing a student
        // - Else, use SharedPreferences email for students
        if (intentEmail != null && intentEmail != sharedPrefEmail) {
            userEmail = intentEmail
            isMentor = true  // Mentor is viewing student's certificates
        } else {
            userEmail = sharedPrefEmail
            isMentor = false // Student viewing their own certificates
        }

        // Debugging Toasts


        // If no email is found, show an error and exit
        if (userEmail == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize certificate category cards
        val MOOCcertificateCard = findViewById<CardView>(R.id.MOOCcertificateCard)
        val ProfessionalcertificateCard = findViewById<CardView>(R.id.ProfessionalcertificateCard)
        val NationalConferencescertificateCard = findViewById<CardView>(R.id.NationalConferencescertificateCard)
        val InternationalConferencescertificateCard = findViewById<CardView>(R.id.InternationalConferencescertificateCard)
        val ExternalEventsTechcertificateCard = findViewById<CardView>(R.id.ExternalEventsTechcertificateCard)
        val ExternalEventsNonTechcertificateCard = findViewById<CardView>(R.id.ExternalEventsNonTechcertificateCard)
        val IndustrialInternshipcertificateCard = findViewById<CardView>(R.id.IndustrialInternshipcertificateCard)

        // Set click listeners for each category card
        MOOCcertificateCard.setOnClickListener { openCertificateList("MOOC Certificates") }
        ProfessionalcertificateCard.setOnClickListener { openCertificateList("Professional Certificates") }
        NationalConferencescertificateCard.setOnClickListener { openCertificateList("National Conferences") }
        InternationalConferencescertificateCard.setOnClickListener { openCertificateList("International Conferences") }
        ExternalEventsTechcertificateCard.setOnClickListener { openCertificateList("External Events(Tech)") }
        ExternalEventsNonTechcertificateCard.setOnClickListener { openCertificateList("External Events(Non-Tech)") }
        IndustrialInternshipcertificateCard.setOnClickListener { openCertificateList("Industrial Internship") }
    }

    private fun openCertificateList(category: String) {
        val intent = Intent(this, CertificateListActivity::class.java)
        intent.putExtra("categoryType", category)
        intent.putExtra("email", userEmail) // Pass the correct email
        startActivity(intent)
    }
}
