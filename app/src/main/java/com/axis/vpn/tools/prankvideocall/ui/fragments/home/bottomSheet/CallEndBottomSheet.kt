package com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.BottomSheetCallEndBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CallEndBottomSheet(
    private val name: String,
    private val imageRes: String,
    private val duration: String
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCallEndBinding? = null
    private val binding get() = _binding!!
    private var selectedRating = 0

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setPadding()

        binding.tvName.text = name
        binding.tvDuration.text = "Call Duration: $duration"

        Glide.with(requireContext())
            .load(imageRes)
            .centerCrop()
            .thumbnail(0.1f)
            .into(binding.ivUser)

        setupRating()
    }
    private fun setupRating() {

        binding.star1.setOnClickListener { onStarClicked(1) }
        binding.star2.setOnClickListener { onStarClicked(2) }
        binding.star3.setOnClickListener { onStarClicked(3) }
        binding.star4.setOnClickListener { onStarClicked(4) }
        binding.star5.setOnClickListener { onStarClicked(5) }
    }

    private fun onStarClicked(rating: Int) {

        // Fill the selected stars.
        updateStars(rating)

        // Optional delay so the user can see the stars being filled.
        Handler(Looper.getMainLooper()).postDelayed({

            if (rating >= 4) {
                openPlayStore()
            } else {
                sendFeedbackMail()
            }

            dismiss()

        }, 300)
    }

    private fun updateStars(rating: Int) {

        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        stars.forEachIndexed { index, imageView ->
            imageView.setImageResource(
                if (index < rating) {
                    R.drawable.ic_star_filled
                } else {
                    R.drawable.ic_star_outline
                }
            )
        }
    }

    private fun openPlayStore() {
        val packageName = requireContext().packageName

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun sendFeedbackMail() {

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:yourmail@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "App Feedback")
        }

        startActivity(Intent.createChooser(intent, "Send Feedback"))
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BottomSheetCallEndBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }


    private fun setPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}