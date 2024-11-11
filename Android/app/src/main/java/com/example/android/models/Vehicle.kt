package com.example.android.models

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.android.CheckoutActivity
import com.example.android.R
import com.example.android.api.Vehicle
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Using for search panel
class VehicleAdapter(
    private val context: Context,
    private val vehicleList: List<Vehicle>
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {
    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vehicleImage: ImageView = itemView.findViewById(R.id.vehicle_image)
        val plateNumber: TextView = itemView.findViewById(R.id.plate_number)
        val entryTime: TextView = itemView.findViewById(R.id.entry_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicleList[position]
        val bitmap = decodeBase64ToBitmap(vehicle.Picture.trim())
        holder.vehicleImage.setImageBitmap(bitmap)
        holder.plateNumber.text = vehicle.plateNumber
        val entryDate = formatDateTime(vehicle.entryTime ?: "")
        holder.entryTime.text = entryDate

        holder.itemView.setOnClickListener {
            showConfirmationDialog(vehicle)
        }
    }

    override fun getItemCount(): Int = vehicleList.size

    private fun showConfirmationDialog(vehicle: Vehicle) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Enter Checkout Page")
            .setMessage("License Plate: ${vehicle.plateNumber}")
            .setPositiveButton("Confirm") { _, _ ->
                val intent = Intent(context, CheckoutActivity::class.java).apply{
                    putExtra("EXTRA_PLATE_NUMBER", vehicle.plateNumber)
                    putExtra("EXTRA_IMAGE_BITMAP", vehicle.Picture)
                    val entryDate = formatDateTime(vehicle.entryTime ?: "")
                    putExtra("EXTRA_IMAGE_ENTRYTIME", entryDate)
                    putExtra("EXTRA_IMAGE_ID", vehicle.imageResId)
                }
                context.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
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

    fun formatDateTime(inputDateTime: String): String {
        val parsedDateTime = ZonedDateTime.parse(inputDateTime, DateTimeFormatter.ISO_DATE_TIME)

        // Format it to the desired MM-dd HH:mm:ss format
        val formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss")
        return parsedDateTime.format(formatter)
    }

//    fun isBase64Format(base64Str: String): Boolean {
//        Log.d("Base64 Check", "Base64 String Length: ${base64Str.length}")
//
//        val base64Pattern = "^[A-Za-z0-9+/=]+$"
//        if (!base64Str.matches(base64Pattern.toRegex())) {
//            return false
//        }
//
//        // Check length for padding
//        if (base64Str.length % 4 != 0) {
//            return false
//        }
//
//        // Try decoding
//        return try {
//            Base64.decode(base64Str, Base64.DEFAULT).isNotEmpty()
//        } catch (e: IllegalArgumentException) {
//            false
//        }
//    }
}