package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AchievementDetailsActivity : AppCompatActivity() {

    private lateinit var idTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var fieldTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var certificateLinkTextView: TextView
    private lateinit var btnUpdate: Button
    private var isMentorViewing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement_details)

        // Initialize views
        idTextView = findViewById(R.id.achievementIdTextView)
        titleTextView = findViewById(R.id.achievementTitleTextView)
        dateTextView = findViewById(R.id.achievementDateTextView)
        fieldTextView = findViewById(R.id.fieldTextView)
        descriptionTextView = findViewById(R.id.achievementDescriptionTextView)
        certificateLinkTextView = findViewById(R.id.certificateLinkTextView)
        btnUpdate = findViewById(R.id.btnUpdate)

        val title = intent.getStringExtra("title") ?: ""
        val field = intent.getStringExtra("field") ?: ""
        val studentEmail = intent.getStringExtra("email") // If mentor is viewing

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("email", null)

        if (loggedInEmail.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            Log.e("AchievementDetailsDebug", "No email found in SharedPreferences")
            finish()
            return
        }

        val emailToFetch = studentEmail ?: loggedInEmail
        isMentorViewing = studentEmail != null

        Log.d("AchievementDetailsDebug", "LoggedInEmail: '$loggedInEmail', StudentEmail: '$studentEmail', Fetching for: '$emailToFetch'")

        if (title.isNotEmpty() && field.isNotEmpty()) {
            fetchAchievementDetails(emailToFetch, title, field)
        } else {
            Toast.makeText(this, "Invalid Achievement Data!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnUpdate.setOnClickListener {
            sendUpdateIntent()
        }
    }

    private fun fetchAchievementDetails(email: String, title: String, field: String) {
        val jsonObject = JSONObject().apply {
            put("email", email)
            put("field", field)
            put("title", title)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://10.46.158.55/online%20mentor/fetch_achievement_details.php")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("AchievementDetailsDebug", "API Request Failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@AchievementDetailsActivity, "Failed to fetch details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.string()?.let { responseBody ->
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.getBoolean("success")) {
                            val achievement = jsonResponse.getJSONObject("achievement")

                            runOnUiThread {
                                idTextView.text = achievement.getInt("id").toString()
                                titleTextView.text = achievement.getString("title")
                                dateTextView.text = achievement.getString("date")
                                fieldTextView.text = achievement.getString("field")
                                descriptionTextView.text = achievement.getString("description")

                                val certificateLink = achievement.optString("certificate_link", "")

                                if (certificateLink.isEmpty()) {
                                    certificateLinkTextView.text = "No Certificate Available"
                                    certificateLinkTextView.isClickable = false
                                } else {
                                    certificateLinkTextView.text = "View Certificate"
                                    certificateLinkTextView.setOnClickListener {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(certificateLink))
                                            startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                this@AchievementDetailsActivity,
                                                "Error opening link",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@AchievementDetailsActivity, "Achievement not found!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AchievementDetailsDebug", "JSON Parsing Error: ${e.message}")
                    }
                }
            }
        })
    }

    private fun sendUpdateIntent() {
        val intent = Intent(this, UpdateAchievementActivity::class.java)

        intent.putExtra("id", idTextView.text.toString().toInt()) // ✅ sends an Int

        intent.putExtra("title", titleTextView.text.toString())
        intent.putExtra("date", dateTextView.text.toString().removePrefix("Date: "))
        intent.putExtra("field", fieldTextView.text.toString().removePrefix("Field: "))
        intent.putExtra("description", descriptionTextView.text.toString().removePrefix("Description: "))
        intent.putExtra("certificate_link", certificateLinkTextView.text.toString())

        startActivity(intent)
    }
}
