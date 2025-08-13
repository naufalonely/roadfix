package edu.unikom.uasproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.unikom.uasproject.adapter.NotificationsAdapter
import edu.unikom.uasproject.databinding.FragmentNotificationsBinding
import edu.unikom.uasproject.model.Notification

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        val notifications = listOf(
            Notification(
                id = "notif001",
                title = "Status Laporan Diperbarui",
                message = "Laporan Anda #rep001 telah selesai diperbaiki.",
                timestamp = "10-08-2025 15:00",
                reportId = "rep001"
            ),
            Notification(
                id = "notif002",
                title = "Notifikasi Penting",
                message = "Terdapat penutupan jalan di area Dago, harap gunakan jalur alternatif.",
                timestamp = "10-08-2025 14:00"
            ),
            Notification(
                id = "notif003",
                title = "Status Laporan Diperbarui",
                message = "Laporan Anda #rep003 sedang dalam proses perbaikan.",
                timestamp = "09-08-2025 11:30",
                reportId = "rep003"
            )
        )

        val adapter = NotificationsAdapter(notifications, this)
        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}