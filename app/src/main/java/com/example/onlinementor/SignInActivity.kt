package com.example.onlinementor

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.graphics.Color
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.button.MaterialButton
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.content.Context
import android.content.SharedPreferences


class SignInActivity : AppCompatActivity() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var signInButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var signUpText: TextView
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make status bar transparent
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.statusBarColor = Color.TRANSPARENT
        
        setContentView(R.layout.activity_sign_in)

        emailEditText = findViewById(R.id.emailInput)
        passwordEditText = findViewById(R.id.passwordInput)
        signInButton = findViewById(R.id.signInButton)
        backButton = findViewById(R.id.backButton)
        signUpText = findViewById(R.id.signUpText)
        emailLayout = findViewById(R.id.emailLayout)
        passwordLayout = findViewById(R.id.passwordLayout)

        backButton.setOnClickListener {
            finish()
        }

        signInButton.setOnClickListener {
            signIn()
        }

        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun signIn() {
        // Reset error states
        emailLayout.error = null
        passwordLayout.error = null

        val email = emailEditText.text?.toString() ?: ""
        val password = passwordEditText.text?.toString() ?: ""

        // Validate inputs
        var isValid = true
        if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            isValid = false
        }
        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        }

        if (isValid) {
            // Show loading state
            signInButton.isEnabled = false
            signInButton.text = "Signing in..."

            val client = OkHttpClient()
            val url = "http://10.46.158.55//online%20mentor/signin.php"

            val json = JSONObject()
            json.put("email", email)
            json.put("password", password)

            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@SignInActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        // Reset button state
                        signInButton.isEnabled = true
                        signInButton.text = "Sign in"
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val responseData = response.body?.string() ?: throw IOException("Empty response body")
                        val jsonResponse = JSONObject(responseData)

                        runOnUiThread {
                            if (jsonResponse.getString("status") == "success") {
                                val dashboard = jsonResponse.getString("dashboard")
                                val signedInEmail = email
                                // Store the signed-in email in session
                                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("email", signedInEmail)
                                editor.apply()
                                // Navigate to the appropriate dashboard based on the response
                                when (dashboard) {
                                    "student_dashboard" -> {
                                        val intent = Intent(this@SignInActivity, StudentDashboardActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    "mentor_dashboard" -> {
                                        val intent = Intent(this@SignInActivity, MentorDashboardActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    "select_role" -> {
                                        val intent = Intent(this@SignInActivity, SelectRoleActivity::class.java).apply {
                                            putExtra("email", email)
                                        }
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            } else {
                                Toast.makeText(this@SignInActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                            // Reset button state
                            signInButton.isEnabled = true
                            signInButton.text = "Sign in"
                        }
                    }
                }
            })
        }
    }
}