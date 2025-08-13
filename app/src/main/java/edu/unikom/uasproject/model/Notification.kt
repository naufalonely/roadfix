package edu.unikom.uasproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val reportId: String? = null
) : Parcelable