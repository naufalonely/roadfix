package edu.unikom.uasproject

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import edu.unikom.uasproject.databinding.FragmentReportDetailBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapDetailView: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        mapDetailView = binding.mapDetailFragment
        Configuration.getInstance().load(requireContext(), androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()))
        mapDetailView.setTileSource(TileSourceFactory.MAPNIK)
        mapDetailView.setMultiTouchControls(true)

        val photoUrl = arguments?.getString("photo_path")
        val status = arguments?.getString("status")
        val damageType = arguments?.getString("damage_type")
        val description = arguments?.getString("description")
        val severity = arguments?.getString("severity")
        val datetime = arguments?.getString("datetime")
        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")
        val locationName = arguments?.getString("location_name")

        if (!photoUrl.isNullOrEmpty()) {
            val file = File(photoUrl)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoUrl)
                binding.ivReportPhoto.setImageBitmap(bitmap)
            } else {
                Glide.with(this).load(photoUrl).into(binding.ivReportPhoto)
            }
        }

        binding.tvReportStatus.text = status
        binding.tvReportDamageType.text = damageType
        binding.tvReportDescription.text = description
        binding.tvReportSeverity.text = severity
        binding.tvReportDatetime.text = datetime

        if (!locationName.isNullOrEmpty()) {
            binding.tvLocationLabel.text = locationName
        }


        if (latitude != null && longitude != null) {
            val locationPoint = GeoPoint(latitude, longitude)
            mapDetailView.controller.setZoom(16.0)
            mapDetailView.controller.setCenter(locationPoint)

            val marker = Marker(mapDetailView)
            marker.position = locationPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            // Perbaiki: Gunakan locationName untuk judul marker
            marker.title = locationName ?: "Lokasi Laporan"
            mapDetailView.overlays.add(marker)
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
        _binding = null
    }
}