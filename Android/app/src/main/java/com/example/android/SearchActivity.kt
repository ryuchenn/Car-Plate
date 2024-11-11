package com.example.android

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.api.ApiClient
import com.example.android.api.CarPlateApi
import com.example.android.api.Vehicle
import com.example.android.api.VehicleResponse
import com.example.android.models.VehicleAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SearchActivity : AppCompatActivity() {

    private lateinit var timeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val api = ApiClient.createService(CarPlateApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Find the TextView
        timeTextView = findViewById(R.id.current_time)
        // Start updating time
        handler.post(updateTimeRunnable)

        // search & clear
        val searchPlateEditText = findViewById<EditText>(R.id.search_plate_number)
        val searchButton = findViewById<Button>(R.id.search_button)
        val clearButton = findViewById<Button>(R.id.clear)

        searchButton.setOnClickListener {
            val query = searchPlateEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchVehicles(query)
            }else{
                fetchAndDisplayVehicleData()
            }
        }

        clearButton.setOnClickListener {
            searchPlateEditText.text.clear() // Clear the text in the EditText
//            Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show()
        }

        // GetData
        fetchAndDisplayVehicleData()

        // Refresh
        val refreshButton = findViewById<Button>(R.id.refresh)
        refreshButton.setOnClickListener {
            fetchAndDisplayVehicleData()
        }
    }

    private fun fetchAndDisplayVehicleData() {
        // Get the RecyclerView
        val vehicleRecyclerView = findViewById<RecyclerView>(R.id.vehicle_recycler_view)
        vehicleRecyclerView.layoutManager = GridLayoutManager(this, 4) // 4 items per row

        api.getVehicles().enqueue(object : Callback<List<VehicleResponse>> {
            override fun onResponse(call: Call<List<VehicleResponse>>, response: Response<List<VehicleResponse>>) {
                if (response.isSuccessful) {
                    val vehicleList = response.body()?.map {
                        Vehicle(it._id, it.licensePlate, it.picture.toString(), it.pictureThumbnail.toString(), it.entryTime)
                    } ?: emptyList()

                    vehicleRecyclerView.adapter = VehicleAdapter(this@SearchActivity, vehicleList)

////                        // Picture
////                        val drawable = resources.getDrawable(R.drawable.testimage1, null)
////                        val bitmap = (drawable as BitmapDrawable).bitmap
////
////                        // Encode
////                        val outputStream = ByteArrayOutputStream()
////                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
////                        val byteArray = outputStream.toByteArray()
////                        val finalBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT)
////
////                        // decode
////                        val decodedBytes = Base64.decode(it.pictureThumbnail.toString(), Base64.DEFAULT)
////                        val ffinalBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
////
////                        // Set Image
////                        val test = findViewById<ImageView>(R.id.imageViewT)
////                        test.setImageBitmap(ffinalBitmap)
                } else {
                    Log.d("SearchActivity", "Failed to fetch vehicle data")
                }
            }

            override fun onFailure(call: Call<List<VehicleResponse>>, t: Throwable) {
                Log.e("SearchActivity", "Error: ${t.message}")
            }
        })
    }

    private fun searchVehicles(query: String) {
        val vehicleRecyclerView = findViewById<RecyclerView>(R.id.vehicle_recycler_view)
        vehicleRecyclerView.layoutManager = GridLayoutManager(this, 4) // 4 items per row

        api.searchVehicles(query).enqueue(object : Callback<List<VehicleResponse>> {
            override fun onResponse(call: Call<List<VehicleResponse>>, response: Response<List<VehicleResponse>>) {
                if (response.isSuccessful) {
                    val vehicleList = response.body()?.map {
                        Vehicle(it._id, it.licensePlate, it.picture.toString(), it.pictureThumbnail.toString(), it.entryTime)
                    } ?: emptyList()

                    // Update RecyclerView adapter with search results
                    vehicleRecyclerView.adapter = VehicleAdapter(this@SearchActivity, vehicleList)
                } else {
                    Log.d("SearchActivity", "Failed to search vehicles")
                }
            }

            override fun onFailure(call: Call<List<VehicleResponse>>, t: Throwable) {
                Log.e("SearchActivity", "Error: ${t.message}")
            }
        })
    }

    private fun getCurrentTime(): String {
        // Format the current time
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("America/Toronto")
        return sdf.format(Date())
    }

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val currentTime = getCurrentTime()
            timeTextView.text = currentTime
            handler.postDelayed(this, 1000)
        }
    }
}
