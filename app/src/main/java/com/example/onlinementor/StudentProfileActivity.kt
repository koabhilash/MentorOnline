package com.example.onlinementor
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import com.example.onlinementor.R



class StudentProfileActivity : AppCompatActivity() {

    private lateinit var tvFullName: TextView
    private lateinit var tvRegNumber: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvDOB: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvBranch: TextView
    private lateinit var tvYear: TextView

    private val PROFILE_URL = "http://10.46.158.55/online%20mentor/display_std.php" // Use actual server URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_profile)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        // Initialize UI elements
        tvFullName = findViewById(R.id.tvFullName)
        tvRegNumber = findViewById(R.id.tvRegNumber)
        tvEmail = findViewById(R.id.tvEmail)
        tvMobile = findViewById(R.id.tvMobile)
        tvDOB = findViewById(R.id.tvDOB)
        tvGender = findViewById(R.id.tvGender)
        tvBranch = findViewById(R.id.tvBranch)
        tvYear = findViewById(R.id.tvYear)

        // Retrieve email from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var email: String? = sharedPreferences.getString("email", null)

        // If email is null, try to get from Intent
        if (email == null) {
            email = intent.getStringExtra("signup_email")
            email?.let {
                sharedPreferences.edit().putString("email", it).apply()
            }
        }
        // Set the email to the TextView
        tvEmail.text = email ?: "Email not available"

        // If email is null, show error and finish activity
        if (email == null) {
            Toast.makeText(this, "Email not found!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            FetchStudentDetailsTask().execute(email)
        }
    }

    private inner class FetchStudentDetailsTask : AsyncTask<String, Void, String?>() {
        override fun doInBackground(vararg params: String): String? {
            return try {
                val url = URL(PROFILE_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.doInput = true

                // Create JSON object to send email
                val jsonParam = JSONObject()
                jsonParam.put("email", params[0])

                // Write JSON data to request body
                val os = OutputStreamWriter(conn.outputStream)
                os.write(jsonParam.toString())
                os.flush()
                os.close()

                // Read response
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val result = reader.readText()
                reader.close()

                result

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(response: String?) {
            if (response != null) {
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getString("status") == "success") {
                        val data = jsonResponse.getJSONObject("data")

                        // Set data in UI
                        tvFullName.text = data.getString("full_name")
                        tvRegNumber.text = data.getString("reg_number")
                        tvEmail.text = data.getString("email")
                        tvMobile.text = data.getString("mobile_number")
                        tvDOB.text = data.getString("date_of_birth")
                        tvGender.text = data.getString("gender")
                        tvBranch.text = data.getString("branch")
                        tvYear.text = getYearName(data.getInt("current_year"))

                    } else {
                        Toast.makeText(this@StudentProfileActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@StudentProfileActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Convert year number to readable format
    private fun getYearName(year: Int): String {
        return when (year) {
            1 -> "First Year"
            2 -> "Second Year"
            3 -> "Third Year"
            4 -> "Final Year"
            else -> "Unknown"
        }
    }
}
