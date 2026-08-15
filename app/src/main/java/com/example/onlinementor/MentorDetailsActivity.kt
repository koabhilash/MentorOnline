package com.example.onlinementor

import android.content.Intent
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.onlinementor.api.ApiClient
import com.example.onlinementor.data.MentorDetailsData
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Context
import android.content.SharedPreferences
import android.widget.ImageView


class MentorDetailsActivity : AppCompatActivity() {
    private lateinit var dobInput: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            Log.d("MentorDetailsActivity", "Setting content view")
            setContentView(R.layout.activity_mentor_details)
            Log.d("MentorDetailsActivity", "Content view set successfully")

            val backArrow = findViewById<ImageView>(R.id.backButton)
            backArrow.setOnClickListener { finish() }

            // Get email from SharedPreferences
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            userEmail = sharedPreferences.getString("email", null)

            if (userEmail == null) {
                Log.e("MentorDetailsActivity", "No email provided")
                Toast.makeText(this, "Error: Session expired", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Initialize views

            val fullNameInput = findViewById<TextInputEditText>(R.id.fullNameInput)
            val IDInput = findViewById<TextInputEditText>(R.id.IDInput)
            val mobileNumberInput = findViewById<TextInputEditText>(R.id.mobileNumberInput)
            dobInput = findViewById(R.id.dobInput)
            val genderInput = findViewById<AutoCompleteTextView>(R.id.genderInput)
            val institutionNameInput = findViewById<TextInputEditText>(R.id.collegeNameInput)
            val branchInput = findViewById<TextInputEditText>(R.id.DepartmentInput)
            val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
            submitButton = findViewById(R.id.submitButton)

            // Prefill the email input field with the signed-in email from SharedPreferences
            val signedInEmail = sharedPreferences.getString("email", null)
            if (signedInEmail != null) {
                emailInput.setText(signedInEmail)
            }

            // Set up date picker
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

            dobInput.setOnClickListener {
                DatePickerDialog(
                    this,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.maxDate = System.currentTimeMillis()
                    calendar.add(Calendar.YEAR, -100)
                    datePicker.minDate = calendar.timeInMillis
                    calendar.add(Calendar.YEAR, 100)
                }.show()
            }

            // Set up dropdown adapters
            val genders = arrayOf("Male", "Female", "Other")

            genderInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders))



            Log.d("MentorDetailsActivity", "Views initialized successfully")

            // Set up click listeners
            submitButton.setOnClickListener {
                if (validateInputs()) {
                    // Disable submit button to prevent multiple submissions
                    submitButton.isEnabled = false

                    val mentorDetails = MentorDetailsData(
                        full_name = fullNameInput.text.toString(),
                        mentor_id = IDInput.text.toString(),
                        mobile_number = mobileNumberInput.text.toString(),
                        date_of_birth = dateFormatter.format(calendar.time),
                        gender = genderInput.text.toString(),
                        college = institutionNameInput.text.toString(),
                        department = branchInput.text.toString(),
                        email = emailInput.text.toString()
                    )

                    lifecycleScope.launch {
                        try {
                            val response = ApiClient.apiService.submitMentorDetails(mentorDetails)
                            Log.d("MentorDetailsActivity", "Response: ${response.body().toString()}") // Log the response
                            if (response.isSuccessful) {
                                val result = response.body()
                                if (result?.status == "success") {
                                    Toast.makeText(this@MentorDetailsActivity, result.message, Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@MentorDetailsActivity, MentorDashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Log.e("MentorDetailsActivity", "Submission failed: ${result?.message ?: "Unknown error occurred"}")
                                    Toast.makeText(this@MentorDetailsActivity, result?.message ?: "Unknown error occurred", Toast.LENGTH_SHORT).show()
                                    submitButton.isEnabled = true
                                }
                            } else {
                                Log.e("MentorDetailsActivity", "Error: ${response.code()} - ${response.message()}")
                                Toast.makeText(this@MentorDetailsActivity, "Failed to submit details. Please try again. Response code: ${response.code()}", Toast.LENGTH_SHORT).show()
                                submitButton.isEnabled = true
                            }
                        } catch (e: Exception) {
                            Log.e("MentorDetailsActivity", "Error submitting details", e)
                            Toast.makeText(this@MentorDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            submitButton.isEnabled = true
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MentorDetailsActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun updateDateInView() {
        dobInput.setText(dateFormatter.format(calendar.time))
    }

    private fun validateInputs(): Boolean {
        val fullNameLayout = findViewById<TextInputLayout>(R.id.fullNameLayout)
        val IDLayout = findViewById<TextInputLayout>(R.id.IDLayout)
        val mobileNumberLayout = findViewById<TextInputLayout>(R.id.mobileNumberLayout)
        val dobLayout = findViewById<TextInputLayout>(R.id.dobLayout)
        val genderLayout = findViewById<TextInputLayout>(R.id.genderLayout)
        val institutionNameLayout = findViewById<TextInputLayout>(R.id.collegeNameLayout)
        val DepartmentLayout = findViewById<TextInputLayout>(R.id.DepartmentLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)

        var isValid = true

        // Reset all errors
        fullNameLayout.error = null
        IDLayout.error = null
        mobileNumberLayout.error = null
        dobLayout.error = null
        genderLayout.error = null
        institutionNameLayout.error = null
        DepartmentLayout.error = null
        emailLayout.error = null

        // Validate Full Name
        if (fullNameLayout.editText?.text.isNullOrEmpty()) {
            fullNameLayout.error = "Please enter your full name"
            isValid = false
        }

        // Validate Registration Number
        if (IDLayout.editText?.text.isNullOrEmpty()) {
            IDLayout.error = "Please enter your registration number"
            isValid = false
        }

        // Validate Mobile Number
        val mobileNumber = mobileNumberLayout.editText?.text.toString()
        if (mobileNumber.isEmpty()) {
            mobileNumberLayout.error = "Please enter your mobile number"
            isValid = false
        } else if (!mobileNumber.matches(Regex("^[0-9]{10}$"))) {
            mobileNumberLayout.error = "Please enter a valid 10-digit mobile number"
            isValid = false
        }

        // Validate Date of Birth
        if (dobLayout.editText?.text.isNullOrEmpty()) {
            dobLayout.error = "Please select your date of birth"
            isValid = false
        }

        // Validate Gender
        if (genderLayout.editText?.text.isNullOrEmpty()) {
            genderLayout.error = "Please select your gender"
            isValid = false
        }

        // Validate Institution Name
        if (institutionNameLayout.editText?.text.isNullOrEmpty()) {
            institutionNameLayout.error = "Please enter your institution name"
            isValid = false
        }

        // Validate Branch
        if (DepartmentLayout.editText?.text.isNullOrEmpty()) {
            DepartmentLayout.error = "Please enter your branch"
            isValid = false
        }

        // Validate Email
        val email = emailLayout.editText?.text.toString()
        if (email.isEmpty()) {
            emailLayout.error = "Please enter your email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Please enter a valid email address"
            isValid = false
        }

        return isValid
    }
}