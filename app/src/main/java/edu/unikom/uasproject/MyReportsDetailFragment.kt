package edu.unikom.uasproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import edu.unikom.uasproject.databinding.FragmentMyReportsDetailBinding
import edu.unikom.uasproject.model.ReportItem

class MyReportsDetailFragment : Fragment() {

    private var _binding: FragmentMyReportsDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReportsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()

        val report = arguments?.getParcelable<ReportItem>("report_data")

        report?.let {
            binding.report = it
            binding.executePendingBindings()

            Glide.with(this)
                .load(it.photoUrl)
                .into(binding.ivReportPhoto)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(report: ReportItem): MyReportsDetailFragment {
            val fragment = MyReportsDetailFragment()
            val args = Bundle()
            args.putParcelable("report_data", report)
            fragment.arguments = args
            return fragment
        }
    }
}