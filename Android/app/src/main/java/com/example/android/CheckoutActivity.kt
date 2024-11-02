package com.example.android
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog

class CheckoutActivity : AppCompatActivity() {
    private lateinit var gestureDetector: GestureDetector
//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val previousPageButton = findViewById<Button>(R.id.button3)
        previousPageButton.setOnClickListener {
            // Navigate back to SearchActivity
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
            finish() // Optional: finish the current activity
        }

        // paying
        val applePayButton = findViewById<Button>(R.id.apple_pay_button)
        val cashPayButton = findViewById<Button>(R.id.cash_pay_button)
        val paymentListener = {
            showProcessingDialog()
        }

        // Set the same listener on both buttons
        applePayButton.setOnClickListener { paymentListener() }
        cashPayButton.setOnClickListener { paymentListener() }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Pass touch events to the gesture detector
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }


    private fun navigateToSearchActivity() {
        // Navigate back to SearchActivity
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish CheckoutActivity
    }

    // paying
    private fun showProcessingDialog() {
        // Create and show an AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setMessage("Paying...")
            .setCancelable(false)
            .create()
        dialog.show()

        // Delay for 3 seconds, then navigate to ReceiptActivity
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss() // Close the dialog
            val intent = Intent(this, ReceiptActivity::class.java)
            startActivity(intent)
            finish() // Finish the current activity
        }, 3000) // 3 seconds
    }
}
