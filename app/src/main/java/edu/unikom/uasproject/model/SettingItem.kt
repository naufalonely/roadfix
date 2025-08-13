package edu.unikom.uasproject.model

data class SettingItem(
    val id: Int,
    val title: String,
    val description: String,
    val iconResId: Int,
    val type: ItemType
)

enum class ItemType {
    PROFILE,
    NOTIFICATION,
    ABOUT,
    LOGOUT
}