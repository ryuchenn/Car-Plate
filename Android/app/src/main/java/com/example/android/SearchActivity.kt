package com.example.android
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SearchActivity : AppCompatActivity() {
    private lateinit var timeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
//    private lateinit var currentTime: TextView
//    private lateinit var searchPlateNumber: EditText
//    private lateinit var searchButton: Button
//
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val nextPageButton = findViewById<Button>(R.id.button4)
        nextPageButton.setOnClickListener {
            // Navigate to CheckoutActivity
            val intent = Intent(this, CheckoutActivity::class.java)
            startActivity(intent)
            println("=========TESTTEST========") // Add a debug log
        }

        // Find the TextView
        timeTextView = findViewById(R.id.current_time)
        // Start updating time
        handler.post(updateTimeRunnable)

        val searchPlateEditText = findViewById<EditText>(R.id.search_plate_number)
        val clearButton = findViewById<Button>(R.id.clear)
        clearButton.setOnClickListener {
            searchPlateEditText.text.clear() // Clear the text in the EditText
//            Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getCurrentTime(): String {
        // Format the current time
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("America/Toronto")
        return sdf.format(Date())
    }

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            // Get current time and format it
            val currentTime = getCurrentTime()
            // Set the formatted time to the TextView
            timeTextView.text = currentTime
            // Schedule the next update in 1 second
            handler.postDelayed(this, 1000)
        }
    }
}
