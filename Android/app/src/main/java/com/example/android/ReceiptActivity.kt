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
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        resetIdleTimer()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetIdleTimer()
    }

    private fun resetIdleTimer() {
        handler.removeCallbacks(idleRunnable)
        handler.postDelayed(idleRunnable, idleTimeout)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(idleRunnable)
    }
}
