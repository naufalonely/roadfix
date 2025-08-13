package edu.unikom.uasproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReportItem(
    val id: String = "",
    val photoUrl: String = "",
    val damageType: String = "",
    val location: String = "",
    val status: String = "",
    val description: String = "",
    val severity: String = "",
    val datetime: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) : Parcelable