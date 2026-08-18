package com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters

import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.ItemHeroBinding
import com.axis.vpn.tools.prankvideocall.databinding.ItemHeroSceduleBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponseSchedule
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
class ScheduleHeroAdapter(
    private val list: List<PrankVideoResponseSchedule>,
    private val onClick: (PrankVideoResponseSchedule) -> Unit,
    private val onDelete: () -> Unit
) : RecyclerView.Adapter<ScheduleHeroAdapter.ViewHolder>() {

    private var scheduledPosition = -1
    private var countDownTimer: CountDownTimer? = null
    private var currentViewHolder: ViewHolder? = null
    private var remainingSeconds = 0

    inner class ViewHolder(
        val binding: ItemHeroSceduleBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemHeroSceduleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = list[position]

        holder.binding.tvHeroName.text = item.name

        // Show initial time
        val totalSeconds =
            if (position == scheduledPosition) {
                remainingSeconds
            } else {
                item.seconds
            }
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        holder.binding.tvCallTime.text =
            String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )

        holder.binding.progressBar.visibility = View.VISIBLE




        Glide.with(holder.itemView.context)
            .load(item.videoUrl)
            .placeholder(R.drawable.loading)
            .error(R.drawable.loading)
            .centerCrop()
            .thumbnail(0.1f)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.binding.progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(holder.binding.ivHero)

        // Update button state
        updateButtonState(holder, position)

        // Schedule button
        holder.binding.btnSchedule.setOnClickListener {
            if (position == scheduledPosition)
                return@setOnClickListener

            countDownTimer?.cancel()

            val previousPosition = scheduledPosition

            scheduledPosition = holder.adapterPosition
            remainingSeconds = item.seconds

            // Update previous item's state directly
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition)
            }

            // Update current item's button state directly (no rebind)
            updateButtonState(holder, position)

            currentViewHolder = holder

            startTimer(item.seconds)

            onClick(item)
        }

        // Delete button
        holder.binding.ivDeleteSchedule.setOnClickListener {
            countDownTimer?.cancel()

            currentViewHolder = null
            remainingSeconds = 0

            val previousPosition = scheduledPosition
            scheduledPosition = -1

            if (previousPosition != -1) {
                notifyItemChanged(previousPosition)
            }

            onDelete()
        }
    }

    /**
     * Updates button state WITHOUT triggering a full rebind
     * This prevents Glide from reloading the image
     */
    private fun updateButtonState(holder: ViewHolder, position: Int) {
        if (position == scheduledPosition) {
            holder.binding.btnSchedule.text =holder.itemView.context.getString(R.string.upcomming)
            holder.binding.ivDeleteSchedule.visibility = View.VISIBLE
            holder.binding.btnSchedule.setBackgroundResource(
                R.drawable.btn_duration_selected_green
            )
        } else {
            holder.binding.btnSchedule.text =holder.itemView.context.getString(R.string.schedule)
            holder.binding.ivDeleteSchedule.visibility = View.GONE
            holder.binding.btnSchedule.setBackgroundResource(
                R.drawable.btn_duration_selected
            )
        }
    }

    private fun startTimer(totalSeconds: Int) {
        countDownTimer?.cancel()

        remainingSeconds = totalSeconds

        countDownTimer = object : CountDownTimer(
            totalSeconds * 1000L,
            1000L
        ) {

            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = (millisUntilFinished / 1000).toInt()

                val hours = remainingSeconds / 3600
                val minutes = (remainingSeconds % 3600) / 60
                val seconds = remainingSeconds % 60

                currentViewHolder?.binding?.tvCallTime?.text =
                    String.format(
                        "%02d:%02d:%02d",
                        hours,
                        minutes,
                        seconds
                    )
            }

            override fun onFinish() {
                remainingSeconds = 0
                currentViewHolder?.binding?.tvCallTime?.text = "00:00:00"
            }

        }.start()
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.binding.progressBar.visibility = View.GONE

        // Clean up timer reference if this was the scheduled item
        if (holder == currentViewHolder) {
            currentViewHolder = null
        }
    }
}