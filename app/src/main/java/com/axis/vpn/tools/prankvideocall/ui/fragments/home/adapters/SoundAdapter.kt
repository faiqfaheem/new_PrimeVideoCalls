package com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.ItemSoundBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.Sound
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.SoundItem
import com.bumptech.glide.Glide

class SoundAdapter(
    private var list: List<Sound>,
    private val onClick: (Sound) -> Unit
) :
    RecyclerView.Adapter<SoundAdapter.ViewHolder>() {


    inner class ViewHolder(
        val binding: ItemSoundBinding
    ) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(

            ItemSoundBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }


    override fun getItemCount() =
        list.size


    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = list[position]

        holder.binding.tvTitle.isSelected=true
        holder.binding.tvTitle.text =
            item.name
        Glide.with(holder.itemView.context)
            .load(item.thumbnail)
            .centerCrop()
            .placeholder(R.drawable.loading)
            .error(R.drawable.loading)
            .into(holder.binding.ivSound)

        holder.binding.root.setOnClickListener {

            onClick(item)

        }

    }


    fun updateList(
        newList: List<Sound>
    ) {

        list = newList

        notifyDataSetChanged()

    }

}