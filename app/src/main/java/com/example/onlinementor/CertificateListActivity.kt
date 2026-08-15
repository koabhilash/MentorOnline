package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
// import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class CertificateListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    // private lateinit var addCertificateButton: Button
    private lateinit var categoryType: String
    private var studentEmail: String? = null  // If mentor is viewing, this will be set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificate_list)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        // addCertificateButton = findViewById(R.id.addCertificateButton)  // Button Reference

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("email", null)  // Logged-in user's email

        categoryType = intent.getStringExtra("categoryType") ?: ""
        studentEmail = intent.getStringExtra("email")  // If mentor is viewing, this will be set

        Log.d("CertificateDebug", "CategoryType: '$categoryType', StudentEmail: '$studentEmail', LoggedInEmail: '$loggedInEmail'")

        // Show "Add Certificate" button only for students (when viewing their own certificates)
        /*
        if (studentEmail == null || studentEmail == loggedInEmail) {
            addCertificateButton.visibility = View.VISIBLE  // Student can add certificates
            Log.d("CertificateDebug", "Student view detected, showing Add Certificate button")
        } else {
            addCertificateButton.visibility = View.GONE  // Mentor viewing a student → Hide button
            Log.d("CertificateDebug", "Mentor view detected, hiding Add Certificate button")
        }
        */

        // If categoryType is empty, exit
        if (categoryType.isBlank()) {
            Toast.makeText(this, "Error: No category selected", Toast.LENGTH_SHORT).show()
            Log.e("CertificateDebug", "No categoryType found in intent")
            finish()
            return
        }

        fetchCertificateTitles()

        // Add Certificate button click (Only visible for students)
        /*
        addCertificateButton.setOnClickListener {
            val intent = Intent(this, CertificateAddActivity::class.java)
            intent.putExtra("categoryType", categoryType)
            startActivity(intent)
        }
        */
    }

    private fun fetchCertificateTitles() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPreferences.getString("email", null)

        val emailToUse = studentEmail ?: loggedInEmail  // If mentor is viewing, use studentEmail

        if (emailToUse == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            Log.e("CertificateDebug", "No email found in SharedPreferences or Intent")
            finish()
            return
        }

        Log.d("CertificateDebug", "Fetching certificates for Email: $emailToUse, Type: $categoryType")

        thread {
            try {
                val url = URL("http://10.46.158.55/online%20mentor/fetch_certificate_titles.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInputString = """
                    {
                        "email": "$emailToUse",
                        "type": "$categoryType"
                    }
                """.trimIndent()

                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonInputString)
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonResponse = JSONObject(response)

                Log.d("CertificateDebug", "Response: $jsonResponse")

                runOnUiThread {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        if (jsonResponse.getBoolean("success")) {
                            val certificates = jsonResponse.getJSONArray("certificates")
                            displayCertificates(certificates)
                        } else {
                            Toast.makeText(this@CertificateListActivity, "No certificates found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@CertificateListActivity, "Server Error: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CertificateListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("CertificateDebug", "Exception: ${e.message}")
                }
            }
        }
    }

    private fun displayCertificates(certificates: JSONArray) {
        val titles = mutableListOf<String>()
        for (i in 0 until certificates.length()) {
            titles.add(certificates.getString(i))
        }

        recyclerView.adapter = CertificateAdapter(titles) { title ->
            openCertificateDetails(title)
        }
    }

    private fun openCertificateDetails(title: String) {
        Log.d("CertificateDebug", "Opening details for: $title, Type: $categoryType, StudentEmail: $studentEmail")

        val intent = Intent(this, CertificateDetailsActivity::class.java)
        intent.putExtra("title", title)
        intent.putExtra("type", categoryType)

        if (studentEmail != null) {
            intent.putExtra("email", studentEmail)
        }

        startActivity(intent)
    }
}
