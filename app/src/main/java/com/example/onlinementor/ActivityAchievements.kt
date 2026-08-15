package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ActivityAchievements : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var studentEmail: String? = null // Used when a mentor is viewing a student's achievements

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("email", null) // Logged-in user email

        // Check if mentor is viewing a student's profile
        studentEmail = intent.getStringExtra("email")

        val emailToFetch = studentEmail ?: loggedInEmail // If mentor is viewing a student, use student email

        Log.d("AchievementsDebug", "LoggedInEmail: '$loggedInEmail', StudentEmail: '$studentEmail', Fetching achievements for: '$emailToFetch'")

        if (emailToFetch == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            Log.e("AchievementsDebug", "No email found in SharedPreferences or Intent")
            finish()
            return
        }

        val categoryCards = mapOf(
            R.id.AcademicsCard to "Academics",
            R.id.SportsCard to "Sports",
            R.id.ArtsCard to "Arts",
            R.id.ResearchCard to "Research",
            R.id.InnovationCard to "Innovation",
            R.id.OthersCard to "Others"
        )

        categoryCards.forEach { (cardId, field) ->
            findViewById<CardView>(cardId).setOnClickListener {
                openAchievementList(field, emailToFetch)
            }
        }
    }

    private fun openAchievementList(field: String, email: String) {
        val intent = Intent(this, AchievementListActivity::class.java).apply {
            putExtra("field", field)
            putExtra("email", email) // Pass correct email
        }
        startActivity(intent)
    }
}
