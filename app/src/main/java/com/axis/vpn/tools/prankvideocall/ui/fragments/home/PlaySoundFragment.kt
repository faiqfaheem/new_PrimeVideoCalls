package com.axis.vpn.tools.prankvideocall.ui.fragments.home

import android.R.attr.type
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.manager.SoundCacheManager
import com.axis.vpn.tools.prankvideocall.databinding.FragmentPlaySoundBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
class PlaySoundFragment : Fragment(R.layout.fragment_play_sound) {

    private var _binding: FragmentPlaySoundBinding? = null
    private val binding get() = _binding!!

    private lateinit var soundCacheManager: SoundCacheManager
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPlaySoundBinding.bind(view)
        soundCacheManager = SoundCacheManager(requireContext())

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        setupViews()
    }

    private fun setupViews() {

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val title = arguments?.getString("title").orEmpty()
        val soundUrl = arguments?.getString("soundUrl").orEmpty()
        val imageUrl = arguments?.getString("thumbnail").orEmpty()

//        binding.tvTitle.text = title

        Glide.with(requireContext())
            .load(imageUrl)
            .centerCrop()
            .into(binding.ivSound)
        binding.tvSoundName.text = title
        binding.tvSoundName.isSelected=true
        if (imageUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .into(binding.ivSound)
        } else {
            binding.ivSound.setImageResource(R.drawable.ic_music)
        }

        binding.btnShare.setOnClickListener {
            shareSound(soundUrl)
        }
        binding.btnPlay.setOnClickListener {
            if (isPlaying) {
                stopSound()
            } else {
                playSound(soundUrl)
            }
        }
    }
    private fun shareSound(soundUrl: String) {

        binding.progressBar.visibility = View.VISIBLE
        binding.btnShare.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val file = if (soundCacheManager.isDownloaded(soundUrl)) {
                    soundCacheManager.getSoundFile(soundUrl)
                } else {
                    soundCacheManager.downloadSound(soundUrl)
                }

                if (!isAdded || _binding == null) return@launch

                binding.progressBar.visibility = View.GONE
                binding.btnShare.isEnabled = true


                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(
                    Intent.createChooser(intent, getString(R.string.share))
                )

            } catch (e: Exception) {

                if (_binding == null) return@launch

                binding.progressBar.visibility = View.GONE
                binding.btnShare.isEnabled = true

                Log.e("SHARE_SOUND", "Share failed", e)

                Toast.makeText(
                    requireContext(),
                    "Unable to share sound",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun playSound(soundUrl: String) {

        binding.progressBar.visibility = View.VISIBLE
        binding.btnPlay.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val file =
                    if (soundCacheManager.isDownloaded(soundUrl)) {
                        soundCacheManager.getSoundFile(soundUrl)
                    } else {
                        soundCacheManager.downloadSound(soundUrl)
                    }

                if (_binding == null || !isAdded) return@launch

                playLocalFile(file.absolutePath)

            } catch (e: Exception) {

                if (_binding == null) return@launch

                binding.progressBar.visibility = View.GONE
                binding.btnPlay.isEnabled = true
            }
        }
    }

    private fun playLocalFile(filePath: String) {

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()

        mediaPlayer?.apply {

            setDataSource(filePath)
            prepare()
            start()

            this@PlaySoundFragment.isPlaying = true

            binding.progressBar.visibility = View.GONE
            binding.btnPlay.isEnabled = true
            binding.btnPlay.text = getString(R.string.pause)
            binding.btnPlay.icon = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.ic_pause
            )

            setOnCompletionListener {
                stopSound()
            }
        }
    }

    private fun stopSound() {

        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }

        mediaPlayer = null
        isPlaying = false

        binding.btnPlay.text = getString(R.string.play)
        binding.btnPlay.icon = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.ic_play
        )
    }


    override fun onPause() {
        super.onPause()

        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }

        mediaPlayer = null
        isPlaying = false

        binding.btnPlay.apply {
            text = getString(R.string.play)
            setIconResource(R.drawable.ic_play)
        }
    }

    override fun onDestroyView() {
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
        super.onDestroyView()
    }
}