package edu.unikom.uasproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import edu.unikom.uasproject.databinding.FragmentHomeBinding
import edu.unikom.uasproject.model.RoadStability
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        val ctx = requireContext()
        Configuration.getInstance().load(ctx, androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx))

        mapView = binding.osmMapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)

        val compassOverlay = CompassOverlay(ctx, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)

        val rotationGestureOverlay = RotationGestureOverlay(mapView)
        rotationGestureOverlay.isEnabled = true
        mapView.overlays.add(rotationGestureOverlay)

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), mapView)
        mapView.overlays.add(myLocationOverlay)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    geocodeAddress(query)
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        binding.fabMyLocation.setOnClickListener {
            findMyLocation()
        }

        binding.fabAddReport.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_reportFragment)
        }

        binding.fabPuprLayer.setOnClickListener {
            showPuprDataDialog()
        }

        checkLocationPermission()
    }

    private fun geocodeAddress(address: String) {
        val url = "https://nominatim.openstreetmap.org/search?q=${address}&format=json&limit=1"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Geocoding failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    try {
                        val jsonArray = JSONArray(it)
                        if (jsonArray.length() > 0) {
                            val jsonObject = jsonArray.getJSONObject(0)
                            val lat = jsonObject.getDouble("lat")
                            val lon = jsonObject.getDouble("lon")

                            activity?.runOnUiThread {
                                val location = GeoPoint(lat, lon)
                                mapView.controller.setCenter(location)
                                mapView.controller.animateTo(location)
                                mapView.controller.setZoom(15.0)
                                Toast.makeText(requireContext(), "Lokasi ditemukan: $address", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Gagal memproses data geocoding", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun showPuprDataDialog() {
        val roadStabilityData = parsePuprCsvData()
        if (roadStabilityData.isNotEmpty()) {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle("Data Kemantapan Jalan Nasional")

            val items = roadStabilityData.map {
                "${it.provinsi}: ${it.mantapPercentage}% Mantap"
            }.toTypedArray()

            dialogBuilder.setItems(items) { _, _ ->
            }
            dialogBuilder.setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.create().show()
        } else {
            Toast.makeText(requireContext(), "Gagal memuat data PUPR.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parsePuprCsvData(): List<RoadStability> {
        val roadStabilityData = mutableListOf<RoadStability>()
        try {
            val inputStream = requireContext().assets.open("Kemantapan Jalan Nasional Tahun 2024.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.readLine() // Skip header
            reader.forEachLine {
                val tokens = it.split(";")
                if (tokens.size >= 7) {
                    val provinsi = tokens[1].trim()
                    val mantapKm = tokens[3].trim()
                    val mantapPercentage = tokens[4].trim()
                    val tidakMantapKm = tokens[5].trim()
                    val tidakMantapPercentage = tokens[6].trim()
                    roadStabilityData.add(RoadStability(provinsi, mantapKm, mantapPercentage, tidakMantapKm, tidakMantapPercentage))
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saat memuat file CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return roadStabilityData
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            myLocationOverlay.enableMyLocation()
            findMyLocation()
        }
    }

    private fun findMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Izin lokasi diperlukan.", Toast.LENGTH_SHORT).show()
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val userLocation = GeoPoint(it.latitude, it.longitude)
                    mapView.controller.animateTo(userLocation)
                } ?: run {
                    Toast.makeText(requireContext(), "Lokasi tidak tersedia.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mendapatkan lokasi.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        myLocationOverlay.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        myLocationOverlay.disableMyLocation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                myLocationOverlay.enableMyLocation()
                findMyLocation()
            } else {
                Toast.makeText(requireContext(), "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}