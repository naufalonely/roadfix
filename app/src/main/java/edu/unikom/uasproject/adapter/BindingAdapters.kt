package edu.unikom.uasproject.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.core.content.ContextCompat

@BindingAdapter("app:srcCompat")
fun setSrcCompat(imageView: ImageView, resourceId: Int) {
    if (resourceId != 0) {
        imageView.setImageResource(resourceId)
    }
}