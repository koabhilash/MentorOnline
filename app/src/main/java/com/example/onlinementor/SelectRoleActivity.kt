package com.example.onlinementor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SelectRoleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_role)



        // Get email from intent
        val email = intent.getStringExtra("email")
        if (email == null) {
            Log.e("SelectRoleActivity", "No email provided")
            Toast.makeText(this, "Error: Session expired", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
            return
        }

        // Initialize role cards
        val parentCard = findViewById<CardView>(R.id.parentCard)
        val mentorCard = findViewById<CardView>(R.id.mentorCard)
        val studentCard = findViewById<CardView>(R.id.studentCard)

        // Set click listeners for each role card
        parentCard.setOnClickListener {
            // Handle parent role selection
            navigateToMain("parent", email)
        }

        mentorCard.setOnClickListener {
            try {
                Log.d("SelectRoleActivity", "Mentor card clicked, email: $email")
                val intent = Intent(this, MentorDetailsActivity::class.java).apply {
                    putExtra("email", email)
                    // Clear back stack to prevent navigation issues
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                Log.d("SelectRoleActivity", "Started MentorDetailsActivity")
                finish()
            } catch (e: Exception) {
                Log.e("SelectRoleActivity", "Error navigating to MentorDetailsActivity", e)
                Toast.makeText(this, "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        studentCard.setOnClickListener {
            try {
                Log.d("SelectRoleActivity", "Student card clicked, email: $email")
                val intent = Intent(this, StudentDetailsActivity::class.java).apply {
                    putExtra("email", email)
                    // Clear back stack to prevent navigation issues
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
                Log.d("SelectRoleActivity", "Started StudentDetailsActivity")
                finish()
            } catch (e: Exception) {
                Log.e("SelectRoleActivity", "Error navigating to StudentDetailsActivity", e)
                Toast.makeText(this, "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToMain(role: String, email: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("selected_role", role)
            putExtra("email", email)
            // Clear back stack to prevent navigation issues
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}