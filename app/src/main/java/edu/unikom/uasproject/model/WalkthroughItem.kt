package edu.unikom.uasproject.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WalkthroughItem(
    val imageResId: Int,
    val title: String,
    val description: String,
    val buttonText: String,
): Parcelable