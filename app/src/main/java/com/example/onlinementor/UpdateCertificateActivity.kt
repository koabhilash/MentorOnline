package com.example.onlinementor

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UpdateCertificateActivity : AppCompatActivity() {

    private lateinit var userEmail: String
    private lateinit var calendar: Calendar
    private lateinit var dateInput: EditText
    private lateinit var storeButton: Button
    private var certificateId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_certificate)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

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

        val titleInput = findViewById<EditText>(R.id.titleInput)
        dateInput = findViewById(R.id.dateInput)
        val durationInput = findViewById<EditText>(R.id.durationInput)
        val certifiedFromInput = findViewById<EditText>(R.id.certifiedFromInput)
        val typeInput = findViewById<Spinner>(R.id.typeInput)
        val certificateLinkInput = findViewById<EditText>(R.id.certificateLinkInput)
        storeButton = findViewById(R.id.storeButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        val types = arrayOf(
            "MOOC Certificates", "Professional Certificates", "National Conferences",
            "International Conferences", "External Events(Tech)", "External Events(Non-Tech)", "Industrial Internship"
        )
        typeInput.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)

        calendar = Calendar.getInstance()

        // ✅ Get data from Intent
        intent?.let {
            certificateId = intent.getIntExtra("id", -1).toString()  // ✅ Convert Int to String after fetching
// Correct key
            titleInput.setText(it.getStringExtra("title"))
            durationInput.setText(it.getStringExtra("duration"))
            certifiedFromInput.setText(it.getStringExtra("certified_from"))
            certificateLinkInput.setText(it.getStringExtra("certificate_link"))

            val type = it.getStringExtra("type")
            val typeIndex = types.indexOf(type)
            if (typeIndex != -1) typeInput.setSelection(typeIndex)

            val dateStr = it.getStringExtra("date")
            if (!dateStr.isNullOrEmpty()) {
                dateInput.setText(dateStr)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = sdf.parse(dateStr)
                parsedDate?.let { calendar.time = it }
            }
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            updateDateInView()
        }

        dateInput.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        backButton.setOnClickListener { finish() }

        storeButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val date = dateInput.text.toString().trim()
            val duration = durationInput.text.toString().trim()
            val certifiedFrom = certifiedFromInput.text.toString().trim()
            val type = typeInput.selectedItem?.toString()?.trim() ?: ""
            val certificateLink = certificateLinkInput.text.toString().trim()

            Log.d("CERT_INPUTS", "title=$title, date=$date, duration=$duration, certifiedFrom=$certifiedFrom, type=$type, link=$certificateLink, email=$userEmail, id=$certificateId")

            if (title.isEmpty() || date.isEmpty() || duration.isEmpty() ||
                certifiedFrom.isEmpty() || type.isEmpty() || certificateLink.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                updateCertificate(title, date, duration, certifiedFrom, type, certificateLink)
            }
        }
    }

    private fun updateDateInView() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateInput.setText(sdf.format(calendar.time))
    }

    private fun updateCertificate(
        title: String,
        date: String,
        duration: String,
        certifiedFrom: String,
        type: String,
        certificateLink: String
    ) {
        val url = "http://10.46.158.55/online%20mentor/update_certificate.php"

        val client = OkHttpClient()
        val json = JSONObject()

        json.put("id", certificateId)
        json.put("email", userEmail)
        json.put("title", title)
        json.put("duration", duration)
        json.put("certified_from", certifiedFrom)
        json.put("type", type)
        json.put("certificate_link", certificateLink)
        json.put("date", date)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Failed to update certificate", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseData ->
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val status = jsonResponse.getString("status")
                        val message = jsonResponse.getString("message")

                        runOnUiThread {
                            if (status == "success") {
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@UpdateCertificateActivity, ActivityCertificate::class.java)
                                startActivity(intent)
                                finish()
                            }
                            else {
                                Toast.makeText(applicationContext, "Error: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Response parsing error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
