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
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.unikom.uasproject.model.ReportItem
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val client = OkHttpClient()

    private lateinit var roadStabilityData: List<RoadStability>

    private val provinceCoordinates = mapOf(
        "ACEH" to GeoPoint(4.6951, 96.7493),
        "SUMATERA UTARA" to GeoPoint(2.1153, 99.5451),
        "SUMATERA BARAT" to GeoPoint(-0.8409, 100.3531),
        "RIAU" to GeoPoint(0.5055, 101.4566),
        "JAMBI" to GeoPoint(-1.6042, 103.6131),
        "SUMATERA SELATAN" to GeoPoint(-3.3194, 104.5779),
        "BENGKULU" to GeoPoint(-3.7925, 102.2618),
        "LAMPUNG" to GeoPoint(-4.5518, 105.4057),
        "KEPULAUAN BANGKA BELITUNG" to GeoPoint(-2.7486, 106.4449),
        "KEPULAUAN RIAU" to GeoPoint(0.9170, 104.4533),
//        "DKI JAKARTA" to GeoPoint(-6.2088, 106.8456),
        "JAWA BARAT" to GeoPoint(-6.9175, 107.6191),
        "JAWA TENGAH" to GeoPoint(-7.2500, 110.0900),
        "D.I. YOGYAKARTA" to GeoPoint(-7.7956, 110.3695),
        "JAWA TIMUR" to GeoPoint(-7.2575, 112.7521),
        "BANTEN" to GeoPoint(-6.4058, 106.0640),
        "BALI" to GeoPoint(-8.3405, 115.0917),
        "NUSA TENGGARA BARAT" to GeoPoint(-8.6527, 117.3616),
        "NUSA TENGGARA TIMUR" to GeoPoint(-8.5833, 120.9167),
        "KALIMANTAN BARAT" to GeoPoint(-0.2546, 111.4550),
        "KALIMANTAN TENGAH" to GeoPoint(-1.6000, 113.8000),
        "KALIMANTAN SELATAN" to GeoPoint(-3.3167, 114.5900),
        "KALIMANTAN TIMUR" to GeoPoint(0.5000, 116.7000),
        "KALIMANTAN UTARA" to GeoPoint(2.8256, 116.9405),
        "SULAWESI UTARA" to GeoPoint(1.4748, 124.8421),
        "SULAWESI TENGAH" to GeoPoint(-1.4300, 121.4116),
        "SULAWESI SELATAN" to GeoPoint(-4.0000, 120.0000),
        "SULAWESI TENGGARA" to GeoPoint(-4.0000, 122.5000),
        "GORONTALO" to GeoPoint(0.7186, 122.8462),
        "SULAWESI BARAT" to GeoPoint(-2.5000, 119.3333),
        "MALUKU" to GeoPoint(-3.2385, 128.0877),
        "MALUKU UTARA" to GeoPoint(0.7891, 127.8153),
        "PAPUA BARAT" to GeoPoint(-2.0000, 132.0000),
        "PAPUA" to GeoPoint(-4.2699, 138.0803),
        "PAPUA SELATAN" to GeoPoint(-7.8549, 137.4925),
        "PAPUA TENGAH" to GeoPoint(-4.2882, 136.2758),
        "PAPUA PEGUNUNGAN" to GeoPoint(-4.1610, 139.0044)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fetchReportsFromFirebase()
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

        roadStabilityData = parsePuprCsvData()

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
        if (roadStabilityData.isNotEmpty()) {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle("Data Kemantapan Jalan Nasional")

            val items = roadStabilityData.map {
                "${it.provinsi}: ${it.mantapPercentage}% Mantap"
            }.toTypedArray()

            dialogBuilder.setItems(items) { dialog, which ->
                val selectedProvince = roadStabilityData[which]
                displayRoadStabilityOnMap(selectedProvince)
                dialog.dismiss()
            }
            dialogBuilder.setPositiveButton("Tutup") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.create().show()
        } else {
            Toast.makeText(requireContext(), "Gagal memuat data PUPR.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayRoadStabilityOnMap(roadStability: RoadStability) {
        mapView.overlays.clear()
        mapView.overlays.add(myLocationOverlay)

        val geoPoint = provinceCoordinates[roadStability.provinsi.uppercase(Locale.getDefault())]
        if (geoPoint != null) {
            mapView.controller.animateTo(geoPoint)
            mapView.controller.setZoom(8.0)

            val marker = Marker(mapView)
            marker.position = geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = "Kemantapan Jalan Nasional"
            marker.snippet = "${roadStability.provinsi}: ${roadStability.mantapPercentage}% Mantap"

            mapView.overlays.add(marker)
            mapView.invalidate()
            Toast.makeText(requireContext(), "Menampilkan data untuk ${roadStability.provinsi}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Koordinat untuk ${roadStability.provinsi} tidak tersedia.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parsePuprCsvData(): List<RoadStability> {
        val roadStabilityData = mutableListOf<RoadStability>()
        try {
            val inputStream = requireContext().assets.open("Kemantapan Jalan Nasional Tahun 2024.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.readLine()
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

    private fun fetchReportsFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val reportsRef = database.getReference("reports")

        reportsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reportsList = mutableListOf<ReportItem>()
                for (reportSnapshot in snapshot.children) {
                    val reportItem = reportSnapshot.getValue(ReportItem::class.java)
                    reportItem?.let {
                        reportsList.add(it)
                    }
                }
                displayReportsOnMap(reportsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat laporan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayReportsOnMap(reports: List<ReportItem>) {
        mapView.overlays.clear()

        mapView.overlays.add(myLocationOverlay)

        for (report in reports) {
            val reportGeoPoint = GeoPoint(report.latitude, report.longitude)
            val reportMarker = Marker(mapView)
            reportMarker.position = reportGeoPoint
            reportMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            reportMarker.title = report.damageType
            reportMarker.snippet = "Status: ${report.status}\nTingkat Keparahan: ${report.severity}"

            val markerIcon: Drawable? = when (report.severity) {
                "Rendah" -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_rendah)
                "Sedang" -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_sedang)
                "Tinggi" -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_tinggi)
                else -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker_default)
            }
            if (markerIcon != null) {
                reportMarker.icon = markerIcon
            }

            mapView.overlays.add(reportMarker)
        }
        mapView.invalidate()
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