package edu.unikom.uasproject.adapter

import edu.unikom.uasproject.model.Notification

interface NotificationClickListener {
    abstract val RetrofitClient: Any

    fun onClick(notification: Notification)
}