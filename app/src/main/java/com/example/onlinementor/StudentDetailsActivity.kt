package com.example.onlinementor;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.lifecycleScope;
import com.example.onlinementor.api.ApiClient;
import com.example.onlinementor.data.StudentDetailsData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import kotlinx.coroutines.launch;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

class StudentDetailsActivity : AppCompatActivity() {
    private lateinit var dobInput: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_student_details)

            val backArrow = findViewById<ImageView>(R.id.backButton)
            backArrow.setOnClickListener { finish() }

            // Retrieve email from SharedPreferences (Sign-in case)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            userEmail = sharedPreferences.getString("email", null)

            // If no email is found in SharedPreferences, check if it is provided via Intent (Sign-up case)
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

            // Initialize views

            val fullNameInput = findViewById<TextInputEditText>(R.id.fullNameInput)
            val regNumberInput = findViewById<TextInputEditText>(R.id.regNumberInput)
            val mobileNumberInput = findViewById<TextInputEditText>(R.id.mobileNumberInput)
            dobInput = findViewById(R.id.dobInput)
            val genderInput = findViewById<AutoCompleteTextView>(R.id.genderInput)
            val institutionNameInput = findViewById<TextInputEditText>(R.id.collegeNameInput)
            val branchInput = findViewById<TextInputEditText>(R.id.branchInput)
            val currentYearInput = findViewById<AutoCompleteTextView>(R.id.currentYearInput)
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
            val currentYearOptions = arrayOf("1st Year", "2nd Year", "3rd Year", "4th Year")
            currentYearInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, currentYearOptions))



            // Set up click listeners for the submit button
            submitButton.setOnClickListener {
                if (validateInputs()) {
                    // Disable submit button to prevent multiple submissions
                    submitButton.isEnabled = false

                    val studentDetails = StudentDetailsData(
                        full_name = fullNameInput.text.toString(),
                        reg_number = regNumberInput.text.toString(),
                        mobile_number = mobileNumberInput.text.toString(),
                        date_of_birth = dateFormatter.format(calendar.time),
                        gender = genderInput.text.toString(),
                        college_name = institutionNameInput.text.toString(),
                        branch = branchInput.text.toString(),
                        current_year = currentYearInput.text.toString(),
                        email = emailInput.text.toString()
                    )


                    lifecycleScope.launch {
                        try {
                            val response = ApiClient.apiService.submitStudentDetails(studentDetails)
                            Log.d("StudentDetailsActivity", "Response: ${response.body().toString()}") // Log the response
                            if (response.isSuccessful) {
                                val result = response.body()
                                if (result?.status == "success") {
                                    Toast.makeText(this@StudentDetailsActivity, result.message, Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@StudentDetailsActivity, StudentDashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Log.e("StudentDetailsActivity", "Submission failed: ${result?.message ?: "Unknown error occurred"}")
                                    Toast.makeText(this@StudentDetailsActivity, result?.message ?: "Unknown error occurred", Toast.LENGTH_SHORT).show()
                                    submitButton.isEnabled = true
                                }
                            } else {
                                Log.e("StudentDetailsActivity", "Error: ${response.code()} - ${response.message()}")
                                Toast.makeText(this@StudentDetailsActivity, "Failed to submit details. Please try again. Response code: ${response.code()}", Toast.LENGTH_SHORT).show()
                                submitButton.isEnabled = true
                            }
                        } catch (e: Exception) {
                            Log.e("StudentDetailsActivity", "Error submitting details", e)
                            Toast.makeText(this@StudentDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            submitButton.isEnabled = true
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("StudentDetailsActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun updateDateInView() {
        dobInput.setText(dateFormatter.format(calendar.time))
    }

    private fun validateInputs(): Boolean {
        Log.d("StudentDetailsActivity", "validateInputs called")
        val fullNameLayout = findViewById<TextInputLayout>(R.id.fullNameLayout)
        val regNumberLayout = findViewById<TextInputLayout>(R.id.regNumberLayout)
        val mobileNumberLayout = findViewById<TextInputLayout>(R.id.mobileNumberLayout)
        val dobLayout = findViewById<TextInputLayout>(R.id.dobLayout)
        val genderLayout = findViewById<TextInputLayout>(R.id.genderLayout)
        val institutionNameLayout = findViewById<TextInputLayout>(R.id.collegeNameLayout)
        val branchLayout = findViewById<TextInputLayout>(R.id.branchLayout)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val currentYearLayout = findViewById<TextInputLayout>(R.id.currentYearLayout)


        var isValid = true

        // Reset all errors
        fullNameLayout.error = null
        regNumberLayout.error = null
        mobileNumberLayout.error = null
        dobLayout.error = null
        genderLayout.error = null
        institutionNameLayout.error = null
        branchLayout.error = null
        emailLayout.error = null
        currentYearLayout.error = null

        // Validate Full Name
        if (fullNameLayout.editText?.text.isNullOrEmpty()) {
            fullNameLayout.error = "Please enter your full name"
            isValid = false
        }

        // Validate Registration Number
        if (regNumberLayout.editText?.text.isNullOrEmpty()) {
            regNumberLayout.error = "Please enter your registration number"
            isValid = false
        }

        // Validate Mobile Number
        val mobileNumber = mobileNumberLayout.editText?.text.toString()
        if (mobileNumber.isEmpty()) {
            mobileNumberLayout.error = "Please enter your mobile number"
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

        // Validate College Name
        if (institutionNameLayout.editText?.text.isNullOrEmpty()) {
            institutionNameLayout.error = "Please enter your college name"
            isValid = false
        }

        // Validate Branch
        if (branchLayout.editText?.text.isNullOrEmpty()) {
            branchLayout.error = "Please enter your branch"
            isValid = false
        }

        // Validate Email
        if (emailLayout.editText?.text.isNullOrEmpty()) {
            emailLayout.error = "Please enter your email"
            isValid = false
        }

        // Validate Current Year
        if (currentYearLayout.editText?.text.isNullOrEmpty()) {
            currentYearLayout.error = "Please select your current year"
            isValid = false
        }


        return isValid
    }
}
