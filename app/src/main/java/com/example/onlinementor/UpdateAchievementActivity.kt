package com.example.onlinementor

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class UpdateAchievementActivity : AppCompatActivity() {

    private lateinit var userEmail: String
    private lateinit var calendar: Calendar
    private lateinit var dateInput: EditText
    private lateinit var storeButton: Button

    private var achievementId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_achievement)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", null)
            ?: intent.getStringExtra("signup_email").orEmpty()

        if (userEmail.isNotEmpty()) {
            sharedPreferences.edit().putString("email", userEmail).apply()
        }

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Get values from intent
        achievementId = intent.getIntExtra("id", -1)
        val titleFromIntent = intent.getStringExtra("title").orEmpty()
        val dateFromIntent = intent.getStringExtra("date").orEmpty()
        val fieldFromIntent = intent.getStringExtra("field").orEmpty()
        val descriptionFromIntent = intent.getStringExtra("description").orEmpty()
        val certificateLinkFromIntent = intent.getStringExtra("certificate_link").orEmpty()

        // Initialize views
        val titleInput = findViewById<EditText>(R.id.titleInput)
        dateInput = findViewById(R.id.dateInput)
        val fieldInput = findViewById<Spinner>(R.id.fieldInput)
        val descriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val certificateLinkInput = findViewById<EditText>(R.id.certificateLinkInput)
        storeButton = findViewById(R.id.storeButton)

        calendar = Calendar.getInstance()

        // Populate initial values
        titleInput.setText(titleFromIntent)
        descriptionInput.setText(descriptionFromIntent)
        certificateLinkInput.setText(certificateLinkFromIntent)
        dateInput.setText(dateFromIntent)

        // Date picker
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

        // Spinner setup
        val fields = arrayOf("Academics", "Sports", "Arts", "Research", "Innovation", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fields)
        fieldInput.adapter = adapter
        val spinnerIndex = fields.indexOf(fieldFromIntent)
        if (spinnerIndex >= 0) fieldInput.setSelection(spinnerIndex)

        // Button click
        storeButton.setOnClickListener {
            val updatedTitle = titleInput.text.toString().trim()
            val updatedDate = dateInput.text.toString().trim()
            val updatedField = fieldInput.selectedItem.toString()
            val updatedDescription = descriptionInput.text.toString().trim()
            val updatedCertificateLink = certificateLinkInput.text.toString().trim()

            if (updatedTitle.isEmpty() || updatedDate.isEmpty() || updatedDescription.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            } else {
                updateAchievement(
                    achievementId,
                    updatedTitle,
                    updatedDate,
                    updatedField,
                    updatedDescription,
                    updatedCertificateLink
                )
            }
        }
    }

    private fun updateDateInView() {
        val format = android.text.format.DateFormat.getDateFormat(applicationContext)
        dateInput.setText(format.format(calendar.time))
    }

    private fun updateAchievement(
        id: Int,
        title: String,
        date: String,
        field: String,
        description: String,
        certificateLink: String
    ) {
        val url = "http://10.46.158.55/online%20mentor/update_achievements.php"

        val json = JSONObject().apply {
            put("id", id)
            put("email", userEmail)
            put("title", title)
            put("date", date)
            put("field", field)
            put("description", description)
            put("certificate_link", certificateLink)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UpdateAchievementActivity, "Failed to update achievement", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                try {
                    val jsonResponse = JSONObject(responseData)
                    val success = jsonResponse.optBoolean("success", false)
                    val message = jsonResponse.optString("message", "Unknown error")

                    runOnUiThread {
                        if (success) {
                            Toast.makeText(applicationContext, "Achievement updated successfully!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@UpdateAchievementActivity, CheckStatusActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(applicationContext, "Error: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error parsing response", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
