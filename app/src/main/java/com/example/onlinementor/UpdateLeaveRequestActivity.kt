package com.example.onlinementor

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UpdateLeaveRequestActivity : AppCompatActivity() {

    private lateinit var userEmail: String
    private lateinit var subjectInput: TextInputEditText
    private lateinit var reasonInput: TextInputEditText
    private lateinit var startDateInput: TextInputEditText
    private lateinit var endDateInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var mentorIdInput: TextInputEditText
    private lateinit var taggingIdInput: TextInputEditText
    private lateinit var proofsInput: TextInputEditText
    private lateinit var permissionLetterInput: TextInputEditText
    private lateinit var submitButton: Button

    private var requestId: String? = null

    private val startDateCalendar = Calendar.getInstance()
    private val endDateCalendar = Calendar.getInstance()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_leave_request)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Find views
        subjectInput = findViewById(R.id.subjectInput)
        reasonInput = findViewById(R.id.reasonInput)
        startDateInput = findViewById(R.id.startDateInput)
        endDateInput = findViewById(R.id.endDateInput)
        emailInput = findViewById(R.id.emailInput)
        mentorIdInput = findViewById(R.id.mentorIdInput)
        taggingIdInput = findViewById(R.id.tagIdInput)
        proofsInput = findViewById(R.id.proofsInput)
        permissionLetterInput = findViewById(R.id.permissionLetterInput)
        submitButton = findViewById(R.id.submitButton)

        // Retrieve email
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", null)
            ?: intent.getStringExtra("signup_email") ?: ""

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sharedPreferences.edit().putString("email", userEmail).apply()
        emailInput.setText(userEmail)
        emailInput.isEnabled = false

        // Get values from intent and prefill
        subjectInput.setText(intent.getStringExtra("subject_to_permission"))
        reasonInput.setText(intent.getStringExtra("reason"))
        startDateInput.setText(intent.getStringExtra("start_date"))
        endDateInput.setText(intent.getStringExtra("end_date"))
        mentorIdInput.setText(intent.getStringExtra("mentor_id"))
        taggingIdInput.setText(intent.getStringExtra("tagging_id"))
        proofsInput.setText(intent.getStringExtra("proofs"))
        permissionLetterInput.setText(intent.getStringExtra("permission_letter"))
        requestId = intent.getStringExtra("id")

        // Prefill calendar dates if possible
        val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        intent.getStringExtra("start_date")?.let {
            try {
                startDateCalendar.time = format.parse(it)!!
            } catch (_: Exception) {
            }
        }
        intent.getStringExtra("end_date")?.let {
            try {
                endDateCalendar.time = format.parse(it)!!
            } catch (_: Exception) {
            }
        }

        // Date picker setup
        val startDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            startDateCalendar.set(Calendar.YEAR, year)
            startDateCalendar.set(Calendar.MONTH, month)
            startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateStartDateInView()
        }

        val endDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            endDateCalendar.set(Calendar.YEAR, year)
            endDateCalendar.set(Calendar.MONTH, month)
            endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateEndDateInView()
        }

        startDateInput.setOnClickListener {
            DatePickerDialog(
                this,
                startDateSetListener,
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        endDateInput.setOnClickListener {
            DatePickerDialog(
                this,
                endDateSetListener,
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = startDateCalendar.timeInMillis
            }.show()
        }

        // Submit
        submitButton.setOnClickListener {
            updateLeaveRequest()
        }
    }

    private fun updateStartDateInView() {
        val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        startDateInput.setText(format.format(startDateCalendar.time))
    }

    private fun updateEndDateInView() {
        val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        endDateInput.setText(format.format(endDateCalendar.time))
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

    private fun updateLeaveRequest() {
        val url = "http://10.46.158.55/online%20mentor/update_request.php"

        if (requestId == null) {
            Toast.makeText(this, "Error: Missing request ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate date logic
        if (startDateCalendar.timeInMillis > endDateCalendar.timeInMillis) {
            Toast.makeText(this, "Start date cannot be after end date", Toast.LENGTH_SHORT).show()
            return
        }

        val json = JSONObject().apply {
            put("id", requestId)
            put("email", userEmail)
            put("subject_to_permission", subjectInput.text.toString())
            put("reason", reasonInput.text.toString())
            put("start_date", startDateInput.text.toString())
            put("end_date", endDateInput.text.toString())
            put("mentor_id", mentorIdInput.text.toString())
            put("tagging_id", taggingIdInput.text.toString())
            put("proofs", proofsInput.text.toString())
            put("permission_letter", permissionLetterInput.text.toString())
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UpdateLeaveRequestActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body?.string()
                runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseStr ?: "")
                        val status = jsonResponse.getString("status")
                        val message = jsonResponse.getString("message")

                        Toast.makeText(this@UpdateLeaveRequestActivity, message, Toast.LENGTH_LONG).show()

                        if (status == "success") {
                            finish()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this@UpdateLeaveRequestActivity, "Invalid response from server", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
}
