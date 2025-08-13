package edu.unikom.uasproject.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import edu.unikom.uasproject.R
import edu.unikom.uasproject.api.ApiService
import edu.unikom.uasproject.model.User
import edu.unikom.uasproject.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

     private var _binding: FragmentProfileBinding? = null
     private val binding get() = _binding!!

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSave: Button

     private lateinit var apiService: ApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        initViews(view)
        setupListeners()
        fetchUserProfile()
        return view
    }

    private fun initViews(view: View) {
        etName = view.findViewById(R.id.et_name)
        etEmail = view.findViewById(R.id.et_email)
        etPassword = view.findViewById(R.id.et_password)
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun fetchUserProfile() {
        val currentUser = User(
            id = "user123",
            name = "Dhini Islamiati Rohman",
            email = "dhini@mail.com"
        )
        displayUserProfile(currentUser)
    }

    private fun displayUserProfile(user: User) {
        etName.setText(user.name)
        etEmail.setText(user.email)
        etPassword.setText("********")
    }

    private fun saveChanges() {
        val newName = etName.text.toString()
        val newEmail = etEmail.text.toString()
        val newPassword = etPassword.text.toString()

        // TODO: Lakukan validasi input (misalnya, email harus valid, password minimal 8 karakter, dll.)

        // TODO: Panggil API untuk memperbarui data
        // Contoh: apiService.updateProfile(newName, newEmail, newPassword)

        Toast.makeText(context, "Perubahan disimpan!", Toast.LENGTH_SHORT).show()

        parentFragmentManager.popBackStack()
    }
}