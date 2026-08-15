package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import org.json.JSONObject

class CertificateDetailsActivity : AppCompatActivity() {

    private lateinit var idTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var certifiedFromTextView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var certificateLinkTextView: TextView
    private lateinit var btnUpdate: Button

    private var userEmail: String? = null
    private var isMentor: Boolean = false

    private var certificateId: Int = -1
    private var certificateTitle = ""
    private var certificateDate = ""
    private var certificateDuration = ""
    private var certificateFrom = ""
    private var certificateType = ""
    private var certificateLink = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificate_details)

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener { finish() }

        // Initialize views
        idTextView = findViewById(R.id.idTextView)
        titleTextView = findViewById(R.id.titleTextView)
        dateTextView = findViewById(R.id.dateTextView)
        durationTextView = findViewById(R.id.durationTextView)
        certifiedFromTextView = findViewById(R.id.certifiedFromTextView)
        typeTextView = findViewById(R.id.typeTextView)
        certificateLinkTextView = findViewById(R.id.certificateLinkTextView)
        btnUpdate = findViewById(R.id.btnUpdate)

        val title = intent.getStringExtra("title")
        val type = intent.getStringExtra("type")

        if (title.isNullOrEmpty() || type.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Invalid certificate details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val sharedPrefEmail = sharedPreferences.getString("email", null)
        val intentEmail = intent.getStringExtra("email")

        userEmail = if (intentEmail != null && intentEmail != sharedPrefEmail) {
            isMentor = true
            intentEmail
        } else {
            isMentor = false
            sharedPrefEmail
        }

        if (userEmail == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchCertificateDetails(title, type)

        btnUpdate.setOnClickListener {
            val intent = Intent(this, UpdateCertificateActivity::class.java)
            intent.putExtra("id", certificateId)
            intent.putExtra("email", userEmail)
            intent.putExtra("title", certificateTitle)
            intent.putExtra("date", certificateDate)
            intent.putExtra("duration", certificateDuration)
            intent.putExtra("certified_from", certificateFrom)
            intent.putExtra("type", certificateType)
            intent.putExtra("certificate_link", certificateLink)
            startActivity(intent)
        }
    }

    private fun fetchCertificateDetails(title: String, type: String) {
        thread {
            try {
                val url = URL("http://10.46.158.55/online%20mentor/fetch_certificate_details.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonInputString = """
                    {
                        "email": "$userEmail",
                        "type": "$type",
                        "title": "$title"
                    }
                """.trimIndent()

                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(jsonInputString)
                outputStream.flush()
                outputStream.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)
                    val jsonResponse = JSONObject(response)

                    runOnUiThread {
                        if (jsonResponse.getBoolean("success")) {
                            val certificate = jsonResponse.getJSONObject("certificate")

                            certificateId = certificate.getInt("id")
                            certificateTitle = certificate.getString("title")
                            certificateDate = certificate.getString("date")
                            certificateDuration = certificate.getString("duration")
                            certificateFrom = certificate.getString("certified_from")
                            certificateType = certificate.getString("type")
                            certificateLink = certificate.getString("certificate_link")

                            idTextView.text = "ID: $certificateId"
                            titleTextView.text = certificateTitle
                            dateTextView.text = "Date: $certificateDate"
                            durationTextView.text = "Duration: $certificateDuration"
                            certifiedFromTextView.text = "Certified From: $certificateFrom"
                            typeTextView.text = "Type: $certificateType"

                            if (certificateLink.isEmpty()) {
                                certificateLinkTextView.text = "No Certificate Link Available"
                                certificateLinkTextView.isClickable = false
                            } else {
                                certificateLinkTextView.text = "View Certificate"
                                certificateLinkTextView.setOnClickListener {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(certificateLink))
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(this@CertificateDetailsActivity, "Error opening link", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@CertificateDetailsActivity, "Certificate not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CertificateDetailsActivity, "Server Error: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@CertificateDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
