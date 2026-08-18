package com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.ItemChatUserBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankChatResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponse
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatUserAdapter(
    private val onClick: (PrankChatResponse) -> Unit
) : RecyclerView.Adapter<ChatUserAdapter.ViewHolder>() {

    private val list = mutableListOf<PrankChatResponse>()

    inner class ViewHolder(
        val binding: ItemChatUserBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemChatUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun submitList(newList: List<PrankChatResponse>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = list[position]

        with(holder.binding) {
            tvName.text = item.name
            tvMessage.text = item.description

            // Display time in hh:mm:ss AM/PM format
            tvTime.text = getFormattedTime(item.seconds)

            // Unread message count
            tvUnread.text = item.messageNo.toString()
            tvUnread.visibility =
                if (item.messageNo > 0) View.VISIBLE else View.GONE

            Glide.with(root.context)
                .asBitmap()
                .load(item.videoUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .frame(1_000_000)
                .centerCrop()
                .into(ivUser)
        }

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }
    private fun getFormattedTime(seconds: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, seconds)

        return SimpleDateFormat(
            "hh:mm:ss a",
            Locale.getDefault()
        ).format(calendar.time)
    }

}