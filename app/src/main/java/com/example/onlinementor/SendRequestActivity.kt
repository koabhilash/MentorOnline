package com.example.onlinementor

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import android.widget.Toast
import android.app.DatePickerDialog
import android.widget.ImageView
import java.util.Calendar
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendRequestActivity : AppCompatActivity() {

    private lateinit var startDateInput: TextInputEditText
    private lateinit var endDateInput: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var emailInput: TextInputEditText
    private var userEmail: String? = null
    private val calendar = Calendar.getInstance()
    private var startDateCalendar = Calendar.getInstance()
    private var endDateCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_request)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        startDateInput = findViewById(R.id.startDateInput)
        endDateInput = findViewById(R.id.endDateInput)
        submitButton = findViewById(R.id.submitButton)
        emailInput = findViewById(R.id.emailInput)

        // Retrieve email from SharedPreferences (similar to StudentDetailsActivity)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", null)

        // If no email is found in SharedPreferences, check if it is provided via Intent
        if (userEmail == null) {
            userEmail = intent.getStringExtra("signup_email")
            // Store email in SharedPreferences if retrieved from sign-up
            if (userEmail != null) {
                with(sharedPreferences.edit()) {
                    putString("email", userEmail)
                    apply()
                }
            }
        }

        // If email is still null, show an error and exit
        if (userEmail == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Prefill the email input field with the signed-in email from SharedPreferences
        emailInput.setText(userEmail)

        // Set up date picker similar to StudentDetailsActivity
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
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() // Allow only future dates
            }.show()
        }

        endDateInput.setOnClickListener {
            DatePickerDialog(
                this,
                endDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = startDateCalendar.timeInMillis // Ensure end date is not before start date
            }.show()
        }

        submitButton.setOnClickListener { submitRequest() }
    }

    private fun updateStartDateInView() {
        val year = startDateCalendar.get(Calendar.YEAR)
        val month = startDateCalendar.get(Calendar.MONTH) + 1 // Month is 0-based
        val day = startDateCalendar.get(Calendar.DAY_OF_MONTH)
        val selectedDate = String.format("%04d-%02d-%02d", year, month, day) // Format to YYYY-MM-DD
        startDateInput.setText(selectedDate)
    }

    private fun updateEndDateInView() {
        val year = endDateCalendar.get(Calendar.YEAR)
        val month = endDateCalendar.get(Calendar.MONTH) + 1 // Month is 0-based
        val day = endDateCalendar.get(Calendar.DAY_OF_MONTH)
        val selectedDate = String.format("%04d-%02d-%02d", year, month, day) // Format to YYYY-MM-DD
        endDateInput.setText(selectedDate)
    }

    private fun submitRequest() {
        val reason = findViewById<TextInputEditText>(R.id.reasonInput).text.toString()
        val startDate = startDateInput.text.toString()
        val endDate = endDateInput.text.toString()
        val email = emailInput.text.toString()
        val mentorId = findViewById<TextInputEditText>(R.id.mentorIdInput).text.toString()
        val taggingId = findViewById<TextInputEditText>(R.id.tagIdInput).text.toString()
        val proofs = findViewById<TextInputEditText>(R.id.proofsInput).text.toString()
        val permissionLetter = findViewById<TextInputEditText>(R.id.permissionLetterInput).text.toString()
        val subjectToPermission = findViewById<TextInputEditText>(R.id.subjectInput).text.toString()

        val requestData = RequestData(
            reason,
            startDate,
            endDate,
            email,
            mentorId,
            taggingId,
            proofs,
            permissionLetter,
            subjectToPermission
        )

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.46.158.55/online%20mentor/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.sendRequest(requestData)
        call.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SendRequestActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                    if (response.body()?.status == "success") {
                        val intent = Intent(this@SendRequestActivity, StudentDashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@SendRequestActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                Toast.makeText(this@SendRequestActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// Define the API interface
interface ApiService {
    @POST("send_request.php")
    fun sendRequest(@Body requestData: RequestData): Call<ResponseData>
}

// Data classes for request and response
data class RequestData(
    val reason: String,
    val start_date: String,
    val end_date: String,
    val email: String,
    val mentor_id: String,
    val tagging_id: String,
    val proofs: String,
    val permission_letter: String,
    val subject_to_permission: String
)

data class ResponseData(
    val status: String,
    val message: String
)
