package edu.unikom.uasproject.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import edu.unikom.uasproject.WalkthroughPageFragment
import edu.unikom.uasproject.model.WalkthroughItem

class WalkthroughPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val walkthroughItems: List<WalkthroughItem>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = walkthroughItems.size

    override fun createFragment(position: Int): Fragment {
        val walkthroughItem = walkthroughItems[position]
        return WalkthroughPageFragment.newInstance(walkthroughItem, position)
    }
}