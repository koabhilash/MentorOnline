package com.example.onlinementor

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MentorPofileDetailsActivity : AppCompatActivity() {

    private lateinit var userEmail: String
    private lateinit var tvFullName: TextView
    private lateinit var tvMentorId: TextView
    private lateinit var tvMobileNumber: TextView
    private lateinit var tvDateOfBirth: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvCollege: TextView
    private lateinit var tvDepartment: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_mentor_details)

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

        tvFullName = findViewById(R.id.tvFullName)
        tvMentorId = findViewById(R.id.tvMentorId)
        tvMobileNumber = findViewById(R.id.tvMobileNumber)
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth)
        tvGender = findViewById(R.id.tvGender)
        tvCollege = findViewById(R.id.tvCollege)
        tvDepartment = findViewById(R.id.tvDepartment)

        fetchMentorDetails(userEmail)
    }

    private fun fetchMentorDetails(email: String) {
        Thread {
            try {
                val url = URL("http://10.46.158.55/online%20mentor/fetch_mentor_details.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonInput = JSONObject()
                jsonInput.put("email", email)

                conn.outputStream.use { os ->
                    os.write(jsonInput.toString().toByteArray())
                }

                val response = conn.inputStream.bufferedReader().use(BufferedReader::readText)

                runOnUiThread {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val mentor = jsonResponse.getJSONObject("mentor")
                        tvFullName.text = " ${mentor.getString("full_name")}"
                        tvMentorId.text = " ${mentor.getString("mentor_id")}"
                        tvMobileNumber.text = "${mentor.getString("mobile_number")}"
                        tvDateOfBirth.text = "${mentor.getString("date_of_birth")}"
                        tvGender.text = "${mentor.getString("gender")}"
                        tvCollege.text = " ${mentor.getString("college")}"
                        tvDepartment.text = " ${mentor.getString("department")}"
                    } else {
                        Toast.makeText(this, "Mentor not found", Toast.LENGTH_SHORT).show()
                    }
                }

                conn.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
