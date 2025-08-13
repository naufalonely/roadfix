package edu.unikom.uasproject

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import edu.unikom.uasproject.adapter.WalkthroughPagerAdapter
import androidx.core.content.edit
import edu.unikom.uasproject.model.WalkthroughItem

class WalkthroughFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNextSkip: Button
    private lateinit var btnStartExploring: Button
    private lateinit var dotsLayout: LinearLayout

    private val walkthroughItems = listOf(
        WalkthroughItem(R.drawable.wt_report, "Laporkan Jalan Rusak dengan Mudah", "Hanya butuh beberapa ketukan untuk membantu menciptakan jalan mulus.", "Lewati"),
        WalkthroughItem(R.drawable.wt_work, "Pantau Progres Perbaikan", "Dapatkan update real-time dan lihat hasil perbaikan laporan Anda.", "Lewati"),
        WalkthroughItem(R.drawable.wt_progress, "Berkontribusi untuk Jalan yang Lebih Baik", "Bersama kita bangun jalan mulus untuk masa depan.", "Mulai Sekarang")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity).supportActionBar?.hide()
        val view = inflater.inflate(R.layout.fragment_walkthrough, container, false)
        viewPager = view.findViewById(R.id.view_pager_walkthrough)
        btnNextSkip = view.findViewById(R.id.btn_next_skip)
        btnStartExploring = view.findViewById(R.id.btn_start_exploring)
        dotsLayout = view.findViewById(R.id.layout_dots)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WalkthroughPagerAdapter(requireActivity(), walkthroughItems)
        viewPager.adapter = adapter

        addDotsIndicator(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                addDotsIndicator(position)
                if (position == walkthroughItems.size - 1) {
                    btnNextSkip.visibility = View.GONE
                    btnStartExploring.visibility = View.VISIBLE
                } else {
                    btnNextSkip.visibility = View.VISIBLE
                    btnStartExploring.visibility = View.GONE
                }
            }
        })

        btnNextSkip.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < walkthroughItems.size - 1) {
                viewPager.currentItem = currentItem + 1
            }
        }

        btnStartExploring.setOnClickListener {
            navigateToMainContent()
        }
    }

    private fun addDotsIndicator(currentPage: Int) {
        dotsLayout.removeAllViews() //
        val dots = arrayOfNulls<TextView>(walkthroughItems.size)

        for (i in dots.indices) {
            dots[i] = TextView(requireContext())
            dots[i]?.text = "•"
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(resources.getColor(R.color.dot_inactive, null))

            dotsLayout.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[currentPage]?.setTextColor(resources.getColor(R.color.dot_active, null))
        }
    }

    private fun navigateToMainContent() {
        val sharedPrefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit { putBoolean("has_seen_walkthrough", true) }
        findNavController().navigate(R.id.action_walkthroughFragment_to_loginFragment)
    }
}