package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity
import com.axis.vpn.tools.prankvideocall.data.viewModels.HomeDataViewModel
import com.axis.vpn.tools.prankvideocall.data.viewModels.StartViewModel
import com.axis.vpn.tools.prankvideocall.databinding.FragmentStartBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.CustomAdapter
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.CustomCallerItem
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters.SuperHeroAdapter
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet.CustomCallerBottomSheet
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet.HeroCallBottomSheet
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet.OnScheduleCallListener
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.SuperHero
import com.axis.vpn.tools.prankvideocall.utils.extension.disableBackPress
import com.axis.vpn.tools.prankvideocall.utils.extension.setEdgeToEdgePadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartFragment :
    Fragment(R.layout.fragment_start),
    OnScheduleCallListener {

    private lateinit var binding: FragmentStartBinding
    private lateinit var heroAdapter: SuperHeroAdapter
    private val homeDataViewModel: HomeDataViewModel by activityViewModels()
    private val viewModel: StartViewModel by viewModel()
    private lateinit var customAdapter: CustomAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentStartBinding.bind(view)

        setupHeroesRecyclerView()
        setupCustomRecyclerView()
        observeCustomCallers()

        customAdapter.submitList(listOf(CustomCallerItem.AddButton))

        homeDataViewModel.preloadData(requireContext())
    }

    private fun setupHeroesRecyclerView() {
        heroAdapter = SuperHeroAdapter { item -> openBottomSheet(item) }

        binding.rvHeroes.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = heroAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }

        homeDataViewModel.videos.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) return@observe
            heroAdapter.submitList(list)
        }
    }

    private fun setupCustomRecyclerView() {
        customAdapter = CustomAdapter(
            onClick = { caller -> openCustomBottomSheet(caller) },
            onDelete = { caller -> viewModel.deleteCaller(caller) },
            onAddContactClick = {
                CustomCallerBottomSheet().show(parentFragmentManager, "CustomCallerBottomSheet")
            }
        )

        binding.rvCustom.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = customAdapter
            setHasFixedSize(true)
            itemAnimator = null

            addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    when (e.actionMasked) {
                        MotionEvent.ACTION_DOWN ->
                            rv.parent.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            rv.parent.requestDisallowInterceptTouchEvent(false)
                    }
                    return false
                }
                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })
        }
    }

    private fun observeCustomCallers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.callers.collect { callers ->
                    val items = mutableListOf<CustomCallerItem>()
                    items.add(CustomCallerItem.AddButton)
                    callers.forEach { caller -> items.add(CustomCallerItem.CallerItem(caller)) }
                    customAdapter.submitList(items)
                }
            }
        }
    }

    private fun openBottomSheet(item: PrankVideoResponse) {
        val bottomSheet = HeroCallBottomSheet.newInstance(
            name = item.name,
            videoUrl = item.videoUrl,
            isCustom = false,
            imagePath = "",
            audioUrl = ""
        )
        bottomSheet.setOnScheduleCallListener(this)
        bottomSheet.show(parentFragmentManager, "HeroCallBottomSheet")
    }

    private fun openCustomBottomSheet(caller: CustomCallerEntity) {
        val bottomSheet = HeroCallBottomSheet.newInstance(
            name = caller.name,
            videoUrl = caller.videoUri,
            isCustom = true,
            imagePath = caller.imagePath,
            audioUrl = caller.audioUri
        )
        bottomSheet.setOnScheduleCallListener(this)
        bottomSheet.show(parentFragmentManager, "HeroCallBottomSheet")
    }

    override fun onScheduleCall(
        heroName: String,
        videoUrl: String,
        delay: Int,
        isCustom: Boolean,
        time: String
    ) {
        Log.d("SCHEDULE_CALL", "onScheduleCall fired: name=$heroName delay=$delay isCustom=$isCustom time=$time")

        val home = parentFragment as? HomeFragment
        if (home == null) {
            Log.e("SCHEDULE_CALL", "parentFragment is NOT HomeFragment — cast failed!")
            return
        }

        home.openScheduleTab(heroName, videoUrl, delay, isCustom, time)
    }
}