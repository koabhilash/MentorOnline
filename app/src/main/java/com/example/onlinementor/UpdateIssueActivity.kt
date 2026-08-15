package com.example.onlinementor

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class UpdateIssueActivity : AppCompatActivity() {

    private lateinit var userEmail: String
    private lateinit var issueDescriptionInput: TextInputEditText
    private lateinit var dateInput: TextInputEditText
    private lateinit var placeInput: TextInputEditText
    private lateinit var moreInfoInput: TextInputEditText
    private lateinit var suggestionsInput: TextInputEditText
    private lateinit var mentorIDInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var docsLinkInput: TextInputEditText
    private lateinit var issueStatusDropdown: MaterialAutoCompleteTextView
    private lateinit var submitButton: Button

    private var issueId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_issue)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        // Enable back arrow in ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Find views
        issueDescriptionInput = findViewById(R.id.issueDescriptionInput)
        dateInput = findViewById(R.id.dateInput)
        placeInput = findViewById(R.id.placeInput)
        moreInfoInput = findViewById(R.id.moreInfoInput)
        suggestionsInput = findViewById(R.id.suggestionsInput)
        mentorIDInput = findViewById(R.id.MentorIDInput)
        emailInput = findViewById(R.id.emailInput)
        docsLinkInput = findViewById(R.id.docslinkInput)
        issueStatusDropdown = findViewById(R.id.issueStatusDropdown)
        submitButton = findViewById(R.id.submitButton)

        // Setup dropdown
        val statusOptions = arrayOf("Pending", "Solved")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusOptions)
        issueStatusDropdown.setAdapter(adapter)

        // Retrieve email
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", null) ?: intent.getStringExtra("signup_email") ?: ""

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sharedPreferences.edit().putString("email", userEmail).apply()
        emailInput.setText(userEmail)
        emailInput.isEnabled = false

        // Get values from intent and prefill
        issueDescriptionInput.setText(intent.getStringExtra("issue_description"))
        dateInput.setText(intent.getStringExtra("report_date"))
        placeInput.setText(intent.getStringExtra("place"))
        moreInfoInput.setText(intent.getStringExtra("more_info"))
        suggestionsInput.setText(intent.getStringExtra("suggestions"))
        mentorIDInput.setText(intent.getStringExtra("mentor_id"))
        docsLinkInput.setText(intent.getStringExtra("file_link"))
        issueStatusDropdown.setText(intent.getStringExtra("issue_status"), false)
        issueId = intent.getStringExtra("issue_id")

        // Open date picker on click
        dateInput.setOnClickListener {
            showDatePicker()
        }

        // Submit button click
        submitButton.setOnClickListener {
            updateIssue()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            dateInput.setText(selectedDate)
        }, year, month, day)

        // Prevent future dates
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateIssue() {
        val url = "http://10.46.158.55/online%20mentor/update_issue.php"

        if (issueId == null) {
            Toast.makeText(this, "Error: Missing issue ID", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("id", issueId)
            put("email", userEmail)
            put("mentor_id", mentorIDInput.text.toString())
            put("issue_description", issueDescriptionInput.text.toString())
            put("report_date", dateInput.text.toString())
            put("place", placeInput.text.toString())
            put("suggestions", suggestionsInput.text.toString())
            put("file_link", docsLinkInput.text.toString())
            put("issue_status", issueStatusDropdown.text.toString())
            put("more_info", moreInfoInput.text.toString())
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UpdateIssueActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseStr ?: "")
                        val status = jsonResponse.getString("status")
                        val message = jsonResponse.getString("message")

                        Toast.makeText(this@UpdateIssueActivity, message, Toast.LENGTH_LONG).show()

                        if (status == "success") {
                            finish()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this@UpdateIssueActivity, "Invalid response from server", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
