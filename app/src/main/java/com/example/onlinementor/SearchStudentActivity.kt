package com.example.onlinementor

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlinementor.R
import com.example.onlinementor.data.Student
import com.example.onlinementor.data.StudentSearchResponse
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SearchStudentActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var studentAdapter: StudentAdapter
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_student)

        val backArrow = findViewById<ImageView>(R.id.backButton)
        backArrow.setOnClickListener { finish() }

        searchInput = findViewById(R.id.searchInput)
        recyclerView = findViewById(R.id.recyclerView)


        recyclerView.layoutManager = LinearLayoutManager(this)
        studentAdapter = StudentAdapter(emptyList()) { student ->
            navigateToCheckStatus(student.email)
        }
        recyclerView.adapter = studentAdapter



        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fetchStudents(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchStudents(query: String) {
        if (query.isEmpty()) {
            studentAdapter.updateData(emptyList())
            return
        }

        val url = "http://10.46.158.55/online%20mentor/search_students.php?query=$query"
        val request = Request.Builder().url(url).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(applicationContext, "Network error", Toast.LENGTH_SHORT).show() }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val jsonObject = JSONObject(it)
                    if (jsonObject.getString("status") == "success") {
                        val studentsArray = jsonObject.getJSONArray("students")
                        val studentList = mutableListOf<Student>()
                        for (i in 0 until studentsArray.length()) {
                            val item = studentsArray.getJSONObject(i)
                            studentList.add(
                                Student(
                                    item.getString("full_name"),
                                    item.getString("reg_number"),
                                    item.getString("email") // Getting the email
                                )
                            )
                        }
                        runOnUiThread { studentAdapter.updateData(studentList) }
                    }
                }
            }
        })
    }

    private fun navigateToCheckStatus(email: String) {
        val intent = Intent(this, CheckStatusActivity::class.java)
        intent.putExtra("student_email", email)
        startActivity(intent)
    }
}
