package com.example.android.models

data class Info(
    val LabelName: String?,
    val EntryTime: String?,
    val OutTime: String?,
    val IsOut: Boolean,
    val ParkingHours: String?,
    val IsPaid: Boolean,
    val Total: Double?,
    val Picture: String?,
    val PictureThumbnail: String?
)
