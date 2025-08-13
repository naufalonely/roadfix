package edu.unikom.uasproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.unikom.uasproject.databinding.FragmentWalkthroughPageBinding
import edu.unikom.uasproject.model.WalkthroughItem

class WalkthroughPageFragment : Fragment() {
    private var _binding: FragmentWalkthroughPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalkthroughPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val walkthroughItem: WalkthroughItem? = arguments?.getParcelable(ARG_WALKTHROUGH_ITEM)
        val pageNumber: Int = arguments?.getInt(ARG_PAGE_NUMBER) ?: 0

        binding.walkthroughItem = walkthroughItem
        binding.pageNumber = pageNumber
        binding.executePendingBindings()

        binding.btnAction.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_WALKTHROUGH_ITEM = "walkthrough_item"
        private const val ARG_PAGE_NUMBER = "page_number"

        @JvmStatic
        fun newInstance(walkthroughItem: WalkthroughItem, pageNumber: Int) =
            WalkthroughPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_WALKTHROUGH_ITEM, walkthroughItem)
                    putInt(ARG_PAGE_NUMBER, pageNumber)
                }
            }
    }
}