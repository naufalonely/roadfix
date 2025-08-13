package edu.unikom.uasproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.unikom.uasproject.R
import edu.unikom.uasproject.databinding.ItemSettingsBinding
import edu.unikom.uasproject.model.ItemType
import edu.unikom.uasproject.model.SettingItem

class SettingAdapter(
    private val items: List<SettingItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<SettingAdapter.SettingViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: SettingItem)
        fun onNotificationToggle(item: SettingItem, isChecked: Boolean)
    }

    inner class SettingViewHolder(private val binding: ItemSettingsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingItem) {
            binding.item = item

            binding.switchNotification.visibility = if (item.type == ItemType.NOTIFICATION) View.VISIBLE else View.GONE

            if (item.type == ItemType.NOTIFICATION) {
                binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
                    listener.onNotificationToggle(item, isChecked)
                }
            } else {
                binding.root.setOnClickListener {
                    listener.onItemClick(item)
                }
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val binding = ItemSettingsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SettingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}