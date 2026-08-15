package com.example.onlinementor

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.onlinementor.api.ApiClient
import com.example.onlinementor.data.SignupRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import android.content.Context



class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up window to draw under system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_sign_up)

        // Initialize views
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val createAccountButton = findViewById<MaterialButton>(R.id.createAccountButton)
        val signInText = findViewById<TextView>(R.id.signInText)
        val emailLayout = findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.passwordLayout)
        val confirmPasswordLayout = findViewById<TextInputLayout>(R.id.confirmPasswordLayout)

        // Set up click listeners
        backButton.setOnClickListener {
            finish()
        }

        createAccountButton.setOnClickListener {
            // Reset errors
            emailLayout.error = null
            passwordLayout.error = null
            confirmPasswordLayout.error = null

            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (validateInputs(email, password, confirmPassword, emailLayout, passwordLayout, confirmPasswordLayout)) {
                createAccountButton.isEnabled = false
                
                lifecycleScope.launch {
                    try {
                        val response = ApiClient.apiService.signup(
                            SignupRequest(email, password, confirmPassword)
                        )

                        if (response.isSuccessful) {
                            val signupResponse = response.body()
                            if (signupResponse?.status == "success") {
                                Toast.makeText(this@SignUpActivity, signupResponse.message, Toast.LENGTH_SHORT).show()
                                // Store signed-up email in SharedPreferences
                                val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("signed_up_email", email)
                                editor.apply()
                                val sharedPreferences2 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor2 = sharedPreferences2.edit()
                                editor2.putString("email", email)
                                editor2.apply()
                                // Pass email to SelectRoleActivity
                                val intent = Intent(this@SignUpActivity, SelectRoleActivity::class.java)
                                intent.putExtra("email", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@SignUpActivity, signupResponse?.message ?: "Unknown error occurred", Toast.LENGTH_SHORT).show()
                                createAccountButton.isEnabled = true
                            }
                        } else {
                            Toast.makeText(this@SignUpActivity, "Failed to sign up. Please try again.", Toast.LENGTH_SHORT).show()
                            createAccountButton.isEnabled = true
                        }
                    } catch (e: Exception) {
                        Log.e("SignUpActivity", "Error during signup", e)
                        Toast.makeText(this@SignUpActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        createAccountButton.isEnabled = true
                    }
                }
            }
        }

        signInText.setOnClickListener {
            finish() // Go back to sign in screen
        }
    }

    private fun validateInputs(
        email: String,
        password: String,
        confirmPassword: String,
        emailLayout: TextInputLayout,
        passwordLayout: TextInputLayout,
        confirmPasswordLayout: TextInputLayout
    ): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailLayout.error = "Please enter your email"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Please enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Please enter your password"
            isValid = false
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }
}