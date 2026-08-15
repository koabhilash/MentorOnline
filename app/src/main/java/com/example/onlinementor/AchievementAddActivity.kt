package com.example.onlinementor

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent


class AchievementAddActivity : AppCompatActivity() {

    private lateinit var userEmail: String
    private lateinit var calendar: Calendar
    private lateinit var dateInput: EditText
    private lateinit var storeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_achievement)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        // Retrieve email from SharedPreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", null) ?: intent.getStringExtra("signup_email").orEmpty()

        // Store email in SharedPreferences if retrieved from Intent
        if (userEmail.isNotEmpty()) {
            with(sharedPreferences.edit()) {
                putString("email", userEmail)
                apply()
            }
        }

        // If email is still null, show an error and exit
        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize UI elements
        val titleInput = findViewById<EditText>(R.id.titleInput)
        dateInput = findViewById(R.id.dateInput)
        val fieldInput = findViewById<Spinner>(R.id.fieldInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val certificateLinkInput = findViewById<EditText>(R.id.certificateLinkInput)
        storeButton = findViewById(R.id.storeButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        calendar = Calendar.getInstance()

        // Date Picker for Date Selection
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        dateInput.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        // Spinner Data for Field Selection
        val fields = arrayOf("Academics", "Sports", "Arts", "Research", "Innovation", "Others")
        fieldInput.adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, fields)

        // Back Button Functionality
        backButton.setOnClickListener {
            finish() // Go back to the previous screen
        }

        // Store Achievement Button Click
        storeButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val date = dateInput.text.toString().trim()
            val field = fieldInput.selectedItem.toString()
            val description = descriptionInput.text.toString().trim()
            val certificateLink = certificateLinkInput.text.toString().trim()

            if (title.isEmpty() || date.isEmpty() || field.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            } else {
                storeAchievement(title, date, field, description, certificateLink)
            }
        }
    }

    // Function to update the date input field
    private fun updateDateInView() {
        val format = android.text.format.DateFormat.getDateFormat(applicationContext)
        dateInput.setText(format.format(calendar.time))
    }

    // Function to store achievement in MySQL via PHP API
    private fun storeAchievement(title: String, date: String, field: String, description: String, certificateLink: String) {
        val url = "http://10.46.158.55/online%20mentor/store_achievement.php" // Replace with your actual PHP API endpoint

        val client = OkHttpClient()
        val json = JSONObject()
        json.put("email", userEmail)
        json.put("title", title)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        json.put("date", formattedDate)
        json.put("field", field)
        json.put("description", description)
        json.put("certificate_link", certificateLink)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to store certificate", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseData ->
                    val jsonResponse = JSONObject(responseData)
                    runOnUiThread {
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(applicationContext, "Achievement stored successfully!", Toast.LENGTH_SHORT).show()

                            // Navigate to CheckStatusActivity
                            val intent = Intent(this@AchievementAddActivity, CheckStatusActivity::class.java)
                            startActivity(intent)
                            finish() // Close the current activity
                        } else {
                            Toast.makeText(applicationContext, "Error: ${jsonResponse.getString("message")}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

    }
}
