package com.example.onlinementor

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class CGPAActivity : AppCompatActivity() {

    private lateinit var userEmail: String
    private lateinit var resultText: TextView
    private lateinit var storeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cgpa)



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
        val inputS = findViewById<EditText>(R.id.inputS)
        val inputA = findViewById<EditText>(R.id.inputA)
        val inputB = findViewById<EditText>(R.id.inputB)
        val inputC = findViewById<EditText>(R.id.inputC)
        val inputD = findViewById<EditText>(R.id.inputD)
        val inputE = findViewById<EditText>(R.id.inputE)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        storeButton = findViewById<Button>(R.id.storeButton) // Store button (Initially hidden)
        resultText = findViewById(R.id.resultText)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Hide store button initially
        storeButton.visibility = View.GONE

        // Back Button Functionality
        backButton.setOnClickListener {
            finish() // Go back to the previous screen
        }

        // Calculate CGPA Button Click
        calculateButton.setOnClickListener {
            val s = inputS.text.toString().toIntOrNull() ?: 0
            val a = inputA.text.toString().toIntOrNull() ?: 0
            val b = inputB.text.toString().toIntOrNull() ?: 0
            val c = inputC.text.toString().toIntOrNull() ?: 0
            val d = inputD.text.toString().toIntOrNull() ?: 0
            val e = inputE.text.toString().toIntOrNull() ?: 0

            val totalGrades = s + a + b + c + d + e
            val totalGradePoints = (s * 10) + (a * 9) + (b * 8) + (c * 7) + (d * 6) + (e * 5)

            if (totalGrades == 0) {
                resultText.text = "Enter valid grades"
                storeButton.visibility = View.GONE
            } else {
                val cgpa = ((totalGradePoints.toDouble() / (10 * totalGrades)) * 10)
                resultText.text = "CGPA: %.2f".format(cgpa)

                // Show store button
                storeButton.visibility = View.VISIBLE

                // Set click listener to store CGPA
                storeButton.setOnClickListener {
                    storeCGPA(cgpa)
                }
            }
        }
    }

    // Function to store CGPA in MySQL via PHP API
    private fun storeCGPA(cgpa: Double) {
        val url = "http://10.46.158.55/online%20mentor/store_cgpa.php" // Replace with your actual PHP API endpoint

        val client = OkHttpClient()
        val json = JSONObject()
        json.put("email", userEmail)
        json.put("cgpa", cgpa)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to store CGPA", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseData ->
                    val jsonResponse = JSONObject(responseData)
                    runOnUiThread {
                        if (jsonResponse.getString("status") == "success") {
                            Toast.makeText(applicationContext, "CGPA stored successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "Error: ${jsonResponse.getString("message")}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
