package edu.unikom.uasproject.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.unikom.uasproject.R
import edu.unikom.uasproject.databinding.ItemReportBinding
import edu.unikom.uasproject.model.ReportItem

class MyReportsAdapter(
    private var reports: List<ReportItem>,
    private val clickListener: ReportClickListener
) : RecyclerView.Adapter<MyReportsAdapter.ReportViewHolder>() {

    class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(report: ReportItem, clickListener: ReportClickListener) {
            binding.report = report
            binding.clickListener = clickListener
            binding.executePendingBindings()

            if (report.photoUrl.isNotEmpty()) {
                Glide.with(binding.ivReportPhotoThumbnail.context)
                    .load(report.photoUrl)
                    .placeholder(R.drawable.ic_gallery_placeholder)
                    .error(R.drawable.ic_error)
                    .into(binding.ivReportPhotoThumbnail)
            } else {
                binding.ivReportPhotoThumbnail.setImageResource(R.drawable.ic_gallery_placeholder)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemReportBinding.inflate(layoutInflater, parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position], clickListener)
    }

    override fun getItemCount(): Int = reports.size

    fun updateReports(newReports: List<ReportItem>) {
        reports = newReports
        notifyDataSetChanged()
    }
}