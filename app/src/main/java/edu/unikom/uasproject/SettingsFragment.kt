package edu.unikom.uasproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import edu.unikom.uasproject.adapter.SettingAdapter
import edu.unikom.uasproject.databinding.FragmentSettingsBinding
import edu.unikom.uasproject.model.ItemType
import edu.unikom.uasproject.model.SettingItem

class SettingsFragment : Fragment(), SettingAdapter.OnItemClickListener {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        val settingItems = listOf(
            SettingItem(1, "Profil Pengguna", "Edit Nama, Email, dan detail lainnya.", R.drawable.ic_user, ItemType.PROFILE),
            SettingItem(2, "Pengaturan Notifikasi", "Toggle untuk mengaktifkan/menonaktifkan jenis notifikasi tertentu", R.drawable.ic_notifications, ItemType.NOTIFICATION),
            SettingItem(3, "Tentang Aplikasi", "Informasi versi, lisensi, dan lain-lain.", R.drawable.ic_about, ItemType.ABOUT),
            SettingItem(4, "Keluar Aplikasi", "Logout dari akun Anda.", R.drawable.ic_logout, ItemType.LOGOUT)
        )

        val adapter = SettingAdapter(settingItems, this)
        binding.rvSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSettings.adapter = adapter
    }

    override fun onItemClick(item: SettingItem) {
        when (item.type) {
            ItemType.PROFILE -> {
                findNavController().navigate(R.id.profileFragment)
                Toast.makeText(context, "Navigasi ke halaman Profil", Toast.LENGTH_SHORT).show()
            }
            ItemType.ABOUT -> {
                showAboutDialog()
            }
            ItemType.LOGOUT -> {
                showLogoutDialog()
            }
            else -> {}
        }
    }

    override fun onNotificationToggle(item: SettingItem, isChecked: Boolean) {
        val message = if (isChecked) "Notifikasi diaktifkan" else "Notifikasi dinonaktifkan"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tentang Aplikasi")
            .setMessage("RoadFix v1.0\n\nAplikasi crowdsourcing untuk melaporkan kerusakan jalan. Dibuat oleh Muhammad Naufal Ghifari. \n\n© 2025.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Ya") { dialog, _ ->
                val firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth.signOut()

                findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}