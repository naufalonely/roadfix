package edu.unikom.uasproject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import edu.unikom.uasproject.api.NominatimClient
import edu.unikom.uasproject.model.ReportItem
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

class ReportFragment : Fragment() {

    private lateinit var photoContainer: FrameLayout
    private lateinit var photoPreview: ImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var spinnerDamageType: Spinner
    private lateinit var spinnerSeverity: Spinner
    private lateinit var btnSubmit: Button
    private lateinit var etDescription: EditText
    private lateinit var progressBar: ProgressBar

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private var reportMarker: Marker? = null
    private var reportLocation: GeoPoint? = null

    private var selectedPhoto: Bitmap? = null

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            getDeviceLocation()
        } else {
            Toast.makeText(context, "Izin lokasi dan kamera diperlukan.", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            selectedPhoto = bitmap
            showImagePreview(bitmap)
            getDeviceLocation()
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                selectedPhoto = imageBitmap
                showImagePreview(imageBitmap)
                getDeviceLocation()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        initViews(view)
        setupListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        Configuration.getInstance().load(requireContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()))
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        checkPermissionsAndGetLocation()
    }

    private fun initViews(view: View) {
        photoContainer = view.findViewById(R.id.photo_container)
        photoPreview = view.findViewById(R.id.iv_photo_preview)
        cameraIcon = view.findViewById(R.id.iv_camera_icon)
        spinnerDamageType = view.findViewById(R.id.spinner_damage_type)
        spinnerSeverity = view.findViewById(R.id.spinner_severity)
        etDescription = view.findViewById(R.id.et_description)
        btnSubmit = view.findViewById(R.id.btn_submit_report)
        mapView = view.findViewById(R.id.map_fragment)
        progressBar = view.findViewById(R.id.progress_bar)

        val damageTypes = listOf("Pilih Jenis Kerusakan", "Berlubang", "Retak", "Genangan Air", "Ambles")
        val severityLevels = listOf("Pilih Tingkat Keparahan", "Rendah", "Sedang", "Tinggi")

        setupSpinner(spinnerDamageType, damageTypes)
        setupSpinner(spinnerSeverity, severityLevels)
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupListeners() {
        photoContainer.setOnClickListener {
            openImageSourceDialog()
        }

        btnSubmit.setOnClickListener {
            submitReport()
        }
    }

    private fun openImageSourceDialog() {
        val options = arrayOf<CharSequence>("Ambil Foto", "Pilih dari Galeri")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Pilih Sumber Gambar")
        builder.setItems(options) { dialog, item ->
            when (item) {
                0 -> {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takePhotoLauncher.launch(cameraIntent)
                }
                1 -> {
                    pickImageLauncher.launch("image/*")
                }
            }
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showImagePreview(bitmap: Bitmap?) {
        bitmap?.let {
            photoPreview.setImageBitmap(it)
            photoPreview.visibility = View.VISIBLE
            cameraIcon.visibility = View.GONE
        }
    }

    private fun checkPermissionsAndGetLocation() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (permissions.all { ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED }) {
            getDeviceLocation()
        } else {
            requestPermissionsLauncher.launch(permissions)
        }
    }

    private fun getDeviceLocation() {
        if (::fusedLocationClient.isInitialized) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = it
                        reportLocation = GeoPoint(it.latitude, it.longitude)
                        updateMapWithLocation(reportLocation)
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Izin lokasi tidak diberikan.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Layanan lokasi belum siap.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMapWithLocation(location: GeoPoint?) {
        location?.let {
            reportLocation = it
            mapView.overlays.clear()

            val newMarker = Marker(mapView)
            newMarker.position = it
            newMarker.title = "Lokasi Laporan"
            mapView.overlays.add(newMarker)
            mapView.controller.animateTo(it)
            mapView.controller.setZoom(16.0)
        }
    }

    private fun submitReport() {
        if (selectedPhoto == null || spinnerDamageType.selectedItemPosition == 0 || etDescription.text.isNullOrBlank() || reportLocation == null) {
            Toast.makeText(context, "Mohon lengkapi semua data laporan.", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        val damageType = spinnerDamageType.selectedItem.toString()
        val severity = spinnerSeverity.selectedItem.toString()
        val description = etDescription.text.toString()
        val latitude = reportLocation!!.latitude
        val longitude = reportLocation!!.longitude
        val datetime = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

        val database = FirebaseDatabase.getInstance("https://roadfix-app-af5f0-default-rtdb.asia-southeast1.firebasedatabase.app")
        val reportsRef = database.getReference("reports")
        val reportId = reportsRef.push().key
        if (reportId == null) {
            Toast.makeText(context, "Gagal membuat ID laporan unik.", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            btnSubmit.isEnabled = true
            return
        }

        val photoPath = saveBitmapToTempFile(selectedPhoto!!, reportId)
        val photoUrl = photoPath

        lifecycleScope.launch {
            try {
                val response = NominatimClient.api.reverseGeocode(latitude, longitude)
                val locationName = if (response.isSuccessful) {
                    response.body()?.displayName ?: "Lokasi Tidak Diketahui"
                } else {
                    "Lokasi Tidak Diketahui"
                }

                val report = ReportItem(
                    id = reportId,
                    photoUrl = photoUrl,
                    damageType = damageType,
                    location = locationName,
                    status = "Terkirim",
                    description = description,
                    severity = severity,
                    datetime = datetime,
                    latitude = latitude,
                    longitude = longitude
                )

                reportsRef
                    .child(reportId)
                    .setValue(report)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        Toast.makeText(requireContext(), "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show()

                        val bundle = Bundle().apply {
                            putString("photo_path", photoUrl)
                            putString("status", "Terkirim")
                            putString("damage_type", damageType)
                            putString("description", description)
                            putString("severity", severity)
                            putDouble("latitude", latitude)
                            putDouble("longitude", longitude)
                            putString("datetime", datetime)
                            putString("location_name", locationName)
                        }
                        findNavController().navigate(R.id.action_reportFragment_to_reportDetailFragment, bundle)
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        Toast.makeText(requireContext(), "Gagal menyimpan laporan", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true
                Toast.makeText(requireContext(), "Gagal mengambil lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap, reportId: String): String {
        val filesDir = requireActivity().applicationContext.filesDir
        val tempFile = File(filesDir, "report_photo_$reportId.png")
        FileOutputStream(tempFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return tempFile.absolutePath
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }
}