package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MentorAccount : AppCompatActivity() {

    private lateinit var tvFullName: TextView
    private lateinit var tvMentorid: TextView
    private lateinit var layoutMyRequests: LinearLayout
    private lateinit var layoutMyStatus: LinearLayout
    private lateinit var layoutMyIssues: LinearLayout
    private lateinit var layoutMyDetails: LinearLayout

    private val PROFILE_URL = "http://10.46.158.55/online%20mentor/mentor_profile_name_id.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentor_account)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        tvFullName = findViewById(R.id.tvFullName)
        tvMentorid = findViewById(R.id.tvMentorid)
        layoutMyRequests = findViewById(R.id.layoutMyRequests)
        layoutMyStatus = findViewById(R.id.layoutMyStatus)
        layoutMyIssues = findViewById(R.id.layoutMyIssues)
        layoutMyDetails = findViewById(R.id.layoutMyDetails)

        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val email: String? = sharedPreferences.getString("email", null)

        if (email == null) {
            Toast.makeText(this, "Email not found!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            FetchStudentDetailsTask().execute(email)
        }

        // Set click listeners for navigation
        layoutMyRequests.setOnClickListener {
            startActivity(Intent(this, RequestHistMen::class.java))
        }

        layoutMyStatus.setOnClickListener {
            startActivity(Intent(this, HistTaggingRequest::class.java))
        }

        layoutMyIssues.setOnClickListener {
            startActivity(Intent(this, IssueHistMentor::class.java))
        }

        layoutMyDetails.setOnClickListener {
            startActivity(Intent(this, MentorPofileDetailsActivity::class.java))
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

                val jsonParam = JSONObject()
                jsonParam.put("email", params[0])

                val os = OutputStreamWriter(conn.outputStream)
                os.write(jsonParam.toString())
                os.flush()
                os.close()

                val responseCode = conn.responseCode
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
                        tvFullName.text = data.getString("full_name")
                        tvMentorid.text = data.getString("mentor_id")
                    } else {
                        Toast.makeText(this@MentorAccount, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@MentorAccount, "Failed to load details", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
