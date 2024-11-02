package com.example.android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class ReceiptActivity : AppCompatActivity() {

    private val idleTimeout: Long = 5000 // 5 seconds delay
    private val handler = Handler(Looper.getMainLooper())
    private val idleRunnable = Runnable {
        // Navigate to StandbyActivity after 5 seconds of inactivity
        val intent = Intent(this, StandbyActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish ReceiptActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        // Start the inactivity timer when the activity is created
        resetIdleTimer()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        // Reset the inactivity timer on any user interaction
        resetIdleTimer()
    }

    private fun resetIdleTimer() {
        // Remove any existing callbacks and post the new delay
        handler.removeCallbacks(idleRunnable)
        handler.postDelayed(idleRunnable, idleTimeout)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the handler when the activity is destroyed
        handler.removeCallbacks(idleRunnable)
    }
}
