package com.example.onlinementor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class StdNotifiDetails : AppCompatActivity() {

    private lateinit var textViewSubject: TextView
    private lateinit var textViewReason: TextView
    private lateinit var textViewStartDate: TextView
    private lateinit var textViewEndDate: TextView
    private lateinit var textViewProofs: TextView
    private lateinit var textViewPermissionLetter: TextView
    private lateinit var textViewStudentName: TextView
    private lateinit var textViewRegNumber: TextView
    private lateinit var textViewTaggingName: TextView
    private lateinit var textViewTaggingId: TextView
    private lateinit var textViewMentorName: TextView
    private lateinit var textViewMentorStatus: TextView
    private lateinit var textViewRequestId: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_std_notifi_details)

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener { finish() }

        textViewSubject = findViewById(R.id.textViewSubject)
        textViewReason = findViewById(R.id.textViewReason)
        textViewStartDate = findViewById(R.id.textViewStartDate)
        textViewEndDate = findViewById(R.id.textViewEndDate)
        textViewProofs = findViewById(R.id.textViewProofs)
        textViewPermissionLetter = findViewById(R.id.textViewPermissionLetter)
        textViewStudentName = findViewById(R.id.textViewStudentName)
        textViewRegNumber = findViewById(R.id.textViewRegNo)
        textViewTaggingName = findViewById(R.id.textViewTaggingName)
        textViewTaggingId = findViewById(R.id.textViewTaggingId)
        textViewMentorName = findViewById(R.id.textViewMentorName)
        textViewMentorStatus = findViewById(R.id.textViewMentorStatus)
        textViewRequestId = findViewById(R.id.textViewId)

        val studentName = intent.getStringExtra("student_name") ?: ""
        val regNumber = intent.getStringExtra("student_reg_number") ?: ""
        val subject = intent.getStringExtra("subject_to_permission") ?: ""
        val mentorName = intent.getStringExtra("mentor_name") ?: ""
        val mentorStatus = intent.getStringExtra("mentor_status") ?: ""
        val startDate = intent.getStringExtra("start_date") ?: ""

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var email = sharedPreferences.getString("email", null)

        if (email == null) {
            email = intent.getStringExtra("signup_email")
            if (email != null) {
                sharedPreferences.edit().putString("email", email).apply()
            }
        }

        if (email == null) {
            Toast.makeText(this, "Error: No email found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchRequestDetails(
            studentName,
            regNumber,
            subject,
            mentorName,
            mentorStatus,
            startDate,
            email
        )
    }

    private fun fetchRequestDetails(
        studentName: String,
        regNumber: String,
        subject: String,
        mentorName: String,
        mentorStatus: String,
        startDate: String,
        email: String
    ) {
        Thread {
            try {
                val url = URL("http://10.46.158.55/online%20mentor/std_ntifi_details.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val jsonParam = JSONObject().apply {
                    put("student_name", studentName)
                    put("student_reg_number", regNumber)
                    put("subject_to_permission", subject)
                    put("mentor_name", mentorName)
                    put("mentor_status", mentorStatus)
                    put("start_date", startDate)
                    put("email", email)
                }

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(jsonParam.toString())
                writer.flush()

                val response = conn.inputStream.bufferedReader().readText()

                runOnUiThread {
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        val data = json.getJSONArray("data").getJSONObject(0)

                        textViewRequestId.text = " ${data.getString("id")}"
                        textViewSubject.text = " ${data.getString("subject_to_permission")}"
                        textViewReason.text = "${data.getString("reason")}"
                        textViewStartDate.text = " ${data.getString("start_date")}"
                        textViewEndDate.text = " ${data.getString("end_date")}"

                        val proofsUrl = data.getString("proofs")
                        val permissionUrl = data.getString("permission_letter")

                        textViewProofs.text = "Open Proofs"
                        textViewProofs.setTextColor(getColor(R.color.purple_500))
                        textViewProofs.paint.isUnderlineText = true
                        textViewProofs.setOnClickListener {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(proofsUrl))
                            startActivity(browserIntent)
                        }

                        textViewPermissionLetter.text = "Open Permission Letter"
                        textViewPermissionLetter.setTextColor(getColor(R.color.purple_500))
                        textViewPermissionLetter.paint.isUnderlineText = true
                        textViewPermissionLetter.setOnClickListener {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(permissionUrl))
                            startActivity(browserIntent)
                        }

                        textViewStudentName.text = "${data.getString("student_name")}"
                        textViewRegNumber.text = " ${data.getString("student_reg_number")}"
                        textViewTaggingName.text = "${data.getString("tagging_name")}"
                        textViewTaggingId.text = " ${data.getString("tagging_id")}"
                        textViewMentorName.text = " $mentorName"
                        textViewMentorStatus.text = "$mentorStatus"
                    } else {
                        Toast.makeText(this, "Error: ${json.getString("message")}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
