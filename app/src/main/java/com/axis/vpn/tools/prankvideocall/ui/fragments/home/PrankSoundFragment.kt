package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.viewModels.HomeDataViewModel
import com.axis.vpn.tools.prankvideocall.databinding.FragmentPrankSoundBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.CategoryAdapter
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.SoundAdapter
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.CategoryResponse
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PrankSoundFragment : Fragment(R.layout.fragment_prank_sound) {

    private lateinit var binding: FragmentPrankSoundBinding
    private val homeDataViewModel: HomeDataViewModel by activityViewModel()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var soundAdapter: SoundAdapter
    private var selectedCategory: CategoryResponse? = null

    // ✅ FIX: Reuse the same touch listener instead of recreating for each RecyclerView
    private val recyclerViewTouchListener = object : RecyclerView.OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> rv.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    rv.parent.requestDisallowInterceptTouchEvent(false)
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPrankSoundBinding.bind(view)

        setupRecyclerViews()
        observeCategories()
        setupSearch()
    }

    private fun setupRecyclerViews() {
        // ✅ FIX 1: Add touch listener only once (reused listener)
        binding.rvCategories.addOnItemTouchListener(recyclerViewTouchListener)

        // ✅ FIX 2: Set RecyclerView optimization flags
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.HORIZONTAL,
                false
            )
            setHasFixedSize(true)  // Layout won't change size
            setItemViewCacheSize(5)  // Cache 5 items for horizontal scroll
        }

        binding.rvSounds.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            setHasFixedSize(true)  // Layout won't change size
            setItemViewCacheSize(8)  // Cache 8 items for grid
        }

        soundAdapter = SoundAdapter(emptyList()) { sound ->
            findNavController().navigate(
                R.id.playSoundFragment,
                bundleOf(
                    "title" to sound.name,
                    "soundUrl" to sound.soundUrl,
                    "thumbnail" to sound.thumbnail
                )
            )
        }

        binding.rvSounds.adapter = soundAdapter
    }

    private fun observeCategories() {
        // ✅ FIX 3: Only update visibility once when data is loaded
        homeDataViewModel.categories.observe(viewLifecycleOwner) { categories ->
            hideLoading()
            setupCategories(categories)
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        // ✅ FIX 4: Use single View.GONE call instead of multiple setVisibility
        arrayOf(
            binding.searchCard,
            binding.rvCategories,
            binding.tvSoundboard,
            binding.rvSounds
        ).forEach { it.visibility = View.GONE }
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        arrayOf(
            binding.searchCard,
            binding.rvCategories,
            binding.tvSoundboard,
            binding.rvSounds
        ).forEach { it.visibility = View.VISIBLE }
    }

    private fun setupCategories(categories: List<CategoryResponse>) {
        // ✅ FIX 5: Reduce list operations
        val mutableList = categories.reversed().toMutableList()

        // Reset selection
        mutableList.forEach { it.isSelected = false }

        // Restore or set default selection
        val selected = selectedCategory?.let { current ->
            mutableList.find { it.id == current.id }
        } ?: mutableList.find { it.name.equals("Trending", true) }

        selected?.isSelected = true
        selectedCategory = selected

        categoryAdapter = CategoryAdapter(mutableList) { category ->
            selectedCategory = category
            categoryAdapter.updateSelection(category.id)
            binding.tvSoundboard.text = "${category.name} Sounds"
            soundAdapter.updateList(category.sounds)
        }

        binding.rvCategories.adapter = categoryAdapter

        selectedCategory?.let {
            binding.tvSoundboard.text = "${it.name} Sounds"
            soundAdapter.updateList(it.sounds)
        }
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { editable ->
            val query = editable.toString().trim()

            // ✅ FIX 6: Filter with null safety and efficient operations
            val filteredList = selectedCategory?.sounds?.filter {
                it.name.contains(query, ignoreCase = true)
            } ?: emptyList()

            soundAdapter.updateList(filteredList)

            // ✅ FIX 7: Use single visibility update
            val shouldShowEmpty = filteredList.isEmpty()
            binding.rvSounds.visibility = if (shouldShowEmpty) View.GONE else View.VISIBLE
            binding.layoutEmpty.visibility = if (shouldShowEmpty) View.VISIBLE else View.GONE
        }
    }
}
