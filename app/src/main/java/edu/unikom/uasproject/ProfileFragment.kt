package edu.unikom.uasproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import edu.unikom.uasproject.api.ApiService
import edu.unikom.uasproject.api.RetrofitClient
import edu.unikom.uasproject.model.User
import edu.unikom.uasproject.databinding.FragmentProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as AppCompatActivity).supportActionBar?.show()
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        apiService = RetrofitClient.instance
        fetchUserProfile()
        return binding.root
    }

    private fun fetchUserProfile() {
        apiService.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        displayUserProfile(it)
                    } ?: run {
                        Toast.makeText(requireContext(), "Data profil tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data profil: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(requireContext(), "Kesalahan jaringan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayUserProfile(user: User) {
        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        binding.etPassword.setText("********")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}