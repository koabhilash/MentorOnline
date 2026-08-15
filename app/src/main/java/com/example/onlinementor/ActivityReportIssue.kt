package com.example.onlinementor

import android.os.Bundle
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.*

class ActivityReportIssue : AppCompatActivity() {

    private lateinit var submitButton: MaterialButton
    private lateinit var issueDescriptionInput: TextInputEditText
    private lateinit var mentorIdInput: TextInputEditText
    private lateinit var placeInput: TextInputEditText
    private lateinit var fileLinkInput: TextInputEditText
    private lateinit var suggestionsInput: TextInputEditText
    private lateinit var moreInfoInput: TextInputEditText
    private lateinit var statusDropdown: AutoCompleteTextView
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val dateInput = findViewById<EditText>(R.id.dateInput)
        submitButton = findViewById(R.id.submitButton)
        issueDescriptionInput = findViewById(R.id.issueDescriptionInput)
        mentorIdInput = findViewById(R.id.MentorIDInput)
        placeInput = findViewById(R.id.placeInput)
        fileLinkInput = findViewById(R.id.docslinkInput)
        suggestionsInput = findViewById(R.id.suggestionsInput)
        moreInfoInput = findViewById(R.id.moreInfoInput)
        statusDropdown = findViewById(R.id.issueStatusDropdown)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userEmail = sharedPreferences.getString("email", null)
        if (userEmail == null) {
            userEmail = intent.getStringExtra("signup_email")
            sharedPreferences.edit().putString("email", userEmail).apply()
        }

        if (userEmail == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        emailInput.setText(userEmail)

        // Date Picker
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                dateInput.setText("$d/${m + 1}/$y")
            }, year, month, day).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        // Set up dropdown
        val issueStatusInput = findViewById<AutoCompleteTextView>(R.id.issueStatusDropdown)
        val issueStatusOptions = arrayOf("Pending", "Solved")
        issueStatusInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, issueStatusOptions))


        submitButton.setOnClickListener { submitIssue() }
    }

    private fun submitIssue() {
        val issueDescription = issueDescriptionInput.text.toString()
        val mentorId = mentorIdInput.text.toString()
        val place = placeInput.text.toString()
        val fileLink = fileLinkInput.text.toString()
        val suggestions = suggestionsInput.text.toString()
        val moreInfo = moreInfoInput.text.toString()
        val status = statusDropdown.text.toString()

        val selectedDate = findViewById<EditText>(R.id.dateInput).text.toString()
        val formattedDate = selectedDate.split("/").let {
            String.format("%04d-%02d-%02d", it[2].toInt(), it[1].toInt(), it[0].toInt())
        }

        val requestData = IssueReportData(
            mentorId,
            issueDescription,
            formattedDate,
            place,
            fileLink,
            suggestions,
            moreInfo,
            status,
            userEmail
        )

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.46.158.55/online%20mentor/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        apiService.submitIssue(requestData).enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                Toast.makeText(this@ActivityReportIssue, response.body()?.message, Toast.LENGTH_SHORT).show()
                if (response.body()?.status == "success") {
                    startActivity(Intent(this@ActivityReportIssue, StudentDashboardActivity::class.java))
                    finish()
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                Toast.makeText(this@ActivityReportIssue, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    interface ApiService {
        @POST("submit_issue.php")
        fun submitIssue(@Body requestData: IssueReportData): Call<ResponseData>
    }

    data class IssueReportData(
        val mentor_id: String,
        val issue_description: String,
        val report_date: String,
        val place: String,
        val file_link: String,
        val suggestions: String?,
        val more_info: String?,
        val issue_status: String,
        val email: String?
    )

    data class ResponseData(
        val status: String,
        val message: String
    )
}
