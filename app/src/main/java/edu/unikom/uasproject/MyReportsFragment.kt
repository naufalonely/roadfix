package edu.unikom.uasproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.unikom.uasproject.adapter.MyReportsAdapter
import edu.unikom.uasproject.adapter.ReportClickListener
import edu.unikom.uasproject.databinding.FragmentMyReportsBinding
import edu.unikom.uasproject.model.ReportItem

class MyReportsFragment : Fragment(), ReportClickListener {

    private var _binding: FragmentMyReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var myReportsAdapter: MyReportsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        setupRecyclerView()
        fetchReportsFromFirebase()
    }

    private fun setupRecyclerView() {
        myReportsAdapter = MyReportsAdapter(emptyList(), this)
        binding.rvReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReports.adapter = myReportsAdapter
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
                myReportsAdapter.updateReports(reportsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Gagal memuat laporan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onClick(report: ReportItem) {
        val bundle = Bundle().apply {
            putParcelable("report_data", report)
        }
        findNavController().navigate(R.id.action_myReportsFragment_to_myReportsDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}