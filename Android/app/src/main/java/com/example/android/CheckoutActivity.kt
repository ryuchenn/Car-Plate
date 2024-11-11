package com.example.android
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.android.api.ApiClient
import com.example.android.api.CarPlateApi
import com.example.android.api.UpdatePaymentRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CheckoutActivity : AppCompatActivity() {
    private lateinit var gestureDetector: GestureDetector
    private val api = ApiClient.createService(CarPlateApi::class.java)


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

        // Set the image and car plate
        val ID = intent.getStringExtra("EXTRA_IMAGE_ID")
        val plateNumber = intent.getStringExtra("EXTRA_PLATE_NUMBER")
        val imageBitmap = intent.getStringExtra("EXTRA_IMAGE_BITMAP")
        val entryTime = intent.getStringExtra("EXTRA_IMAGE_ENTRYTIME") ?: "01-01 00:00:00" // MM-DD HH:mm:ss
        val entryDateTime  = parseToDateTime(entryTime)    // YYYY-MM-DD HH:mm:ss

        val bitmap = imageBitmap?.let { decodeBase64ToBitmap(it) }
        val carImageView = findViewById<ImageView>(R.id.checkout_car_image)
        val plateNumberTextView = findViewById<TextView>(R.id.checkout_license_plate)
        val entryTimeTextView = findViewById<TextView>(R.id.entry_time)
        val parkingDurationTextView = findViewById<TextView>(R.id.parking_duration)
        val totalAmountTextView = findViewById<TextView>(R.id.total_amount)

        val timeDifference = calculateTimeDifference(entryDateTime) // X Days Y hours Z mins
        val duration = Duration.between(entryDateTime, LocalDateTime.now()) // PT13H53M36.786231S
        val parkingFee = calculateParkingFee(duration)

        // paying button
        val applePayButton = findViewById<Button>(R.id.apple_pay_button)
        val cashPayButton = findViewById<Button>(R.id.cash_pay_button)
        val paymentListener = {
            showProcessingDialog(ID ?: "", parkingFee, timeDifference)
        }

        // Set the same listener on both buttons
        applePayButton.setOnClickListener { paymentListener() }
        cashPayButton.setOnClickListener { paymentListener() }

        // Set images and car plate
        if (bitmap != null) {
            carImageView.setImageBitmap(bitmap)
        }
        plateNumberTextView.text = "License Plate: $plateNumber"
        entryTimeTextView.text = "Entry Time: $entryTime"
        parkingDurationTextView.text = "Parking Duration: $timeDifference"
        totalAmountTextView.text = "Parking Duration: $$parkingFee"
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str.trim(), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun parseToDateTime(dateString: String): LocalDateTime {
        val currentYear = LocalDateTime.now().year
        val fullDateString = "$currentYear-$dateString"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.parse(fullDateString, formatter)
    }

    fun calculateTimeDifference(startDateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val duration = Duration.between(startDateTime, now)

        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60

        return "${days} Days ${hours} hours ${minutes} mins"
    }

    fun calculateParkingFee(duration: Duration): Int {
        val totalHours = duration.toHours().toInt()

        return when {
            totalHours < 1 -> 5 // Minimum fee for less than an hour
            totalHours <= 6 -> (totalHours * 5).coerceAtMost(30) // 5 per hour, max 30
            else -> 30 // Maximum charge for more than 6 hours
        }
    }

    // paying
    private fun showProcessingDialog(ID: String, parkingFee: Int, parkingDuration: String) {
        val paymentRequest = UpdatePaymentRequest(
            _id = ID,
            total = parkingFee,
            parkingHours = parkingDuration
        )

        api.updatePayment(ID, paymentRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("SearchActivity", "Payment updated successfully")
                } else {
                    Log.d("SearchActivity", "Failed to update payment")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SearchActivity", "Error updating payment: ${t.message}")
            }
        })


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
