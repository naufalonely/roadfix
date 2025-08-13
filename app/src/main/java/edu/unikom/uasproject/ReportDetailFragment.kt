package edu.unikom.uasproject

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import edu.unikom.uasproject.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportDetailFragment : Fragment() {

    private lateinit var reportPhoto: ImageView
    private lateinit var reportStatus: TextView
    private lateinit var reportDateTime: TextView
    private lateinit var reportDamageType: TextView
    private lateinit var reportDescription: TextView
    private lateinit var reportSeverity: TextView
    private lateinit var mapDetailView: MapView

    private var reportLocation: GeoPoint? = null
    private var photoPath: String? = null

    private var status: String = "Terkirim"
    private var datetime: String = ""
    private var damageType: String = ""
    private var description: String = ""
    private var severity: String = ""

    companion object {
        private const val ARG_PHOTO_PATH = "photo_path"
        private const val ARG_STATUS = "status"
        private const val ARG_DATETIME = "datetime"
        private const val ARG_DAMAGE_TYPE = "damage_type"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_SEVERITY = "severity"
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(
            photoPath: String, status: String, damageType: String, description: String,
            severity: String, latitude: Double, longitude: Double
        ): ReportDetailFragment {
            val fragment = ReportDetailFragment()
            val args = Bundle().apply {
                putString(ARG_PHOTO_PATH, photoPath)
                putString(ARG_STATUS, status)
                putString(ARG_DATETIME, SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date()))
                putString(ARG_DAMAGE_TYPE, damageType)
                putString(ARG_DESCRIPTION, description)
                putString(ARG_SEVERITY, severity)
                putDouble(ARG_LATITUDE, latitude)
                putDouble(ARG_LONGITUDE, longitude)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report_detail, container, false)
        initViews(view)
        getArgumentsData()
        displayData()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        Configuration.getInstance().load(requireContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()))
        mapDetailView.setTileSource(TileSourceFactory.MAPNIK)
        mapDetailView.controller.setZoom(16.0)

        displayMapLocation()
    }

    private fun initViews(view: View) {
        reportPhoto = view.findViewById(R.id.iv_report_photo)
        reportStatus = view.findViewById(R.id.tv_report_status)
        reportDateTime = view.findViewById(R.id.tv_report_datetime)
        reportDamageType = view.findViewById(R.id.tv_report_damage_type)
        reportDescription = view.findViewById(R.id.tv_report_description)
        reportSeverity = view.findViewById(R.id.tv_report_severity)
        mapDetailView = view.findViewById(R.id.map_detail_fragment)
    }

    private fun getArgumentsData() {
        arguments?.let {
            photoPath = it.getString(ARG_PHOTO_PATH)
            status = it.getString(ARG_STATUS) ?: "Terkirim"
            datetime = it.getString(ARG_DATETIME) ?: ""
            damageType = it.getString(ARG_DAMAGE_TYPE) ?: ""
            description = it.getString(ARG_DESCRIPTION) ?: ""
            severity = it.getString(ARG_SEVERITY) ?: ""
            val lat = it.getDouble(ARG_LATITUDE)
            val lon = it.getDouble(ARG_LONGITUDE)
            reportLocation = GeoPoint(lat, lon)
        }
    }

    private fun displayData() {
        photoPath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            reportPhoto.setImageBitmap(bitmap)
        }

        reportStatus.text = status
        reportDateTime.text = "Tanggal dan Waktu: $datetime"
        reportDamageType.text = "Jenis Kerusakan: $damageType"
        reportDescription.text = "Deskripsi: $description"
        reportSeverity.text = "Tingkat Keparahan: $severity"
    }

    private fun displayMapLocation() {
        reportLocation?.let {
            mapDetailView.overlays.clear()

            val reportMarker = Marker(mapDetailView)
            reportMarker.position = it
            reportMarker.title = "Lokasi Laporan"
            mapDetailView.overlays.add(reportMarker)
            mapDetailView.controller.setCenter(it)
        }
    }

    override fun onResume() {
        super.onResume()
        mapDetailView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapDetailView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapDetailView.onDetach()
    }
}