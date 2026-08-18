package com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.databinding.ItemHeroBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.SuperHero
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener


class SuperHeroAdapter(
    private val onClick: (PrankVideoResponse) -> Unit
) : RecyclerView.Adapter<SuperHeroAdapter.ViewHolder>() {

    private val list =
        mutableListOf<PrankVideoResponse>()

    fun submitList(
        newList: List<PrankVideoResponse>
    ) {

        list.clear()
        list.addAll(newList)

        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemHeroBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemHeroBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = list[position]

        holder.binding.tvName.text = item.name

        // Cancel previous request for recycled ViewHolder
        Glide.with(holder.itemView.context)
            .clear(holder.binding.ivHero)

        holder.binding.progressBar.visibility =
            View.VISIBLE

        Glide.with(holder.itemView.context)
            .load(item.videoUrl)
            .centerCrop()
            .diskCacheStrategy(
                DiskCacheStrategy.ALL
            )
            .thumbnail(0.1f)
            .listener(
                object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.progressBar.visibility =
                        View.GONE

                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.progressBar.visibility =
                            View.GONE

                        return false
                    }
                }
            )
            .into(holder.binding.ivHero)

        holder.itemView.setOnClickListener {

            if (holder.bindingAdapterPosition !=
                RecyclerView.NO_POSITION
            ) {

                onClick(item)
            }
        }
    }

    override fun onViewRecycled(
        holder: ViewHolder
    ) {
        super.onViewRecycled(holder)

        Glide.with(holder.itemView.context)
            .clear(holder.binding.ivHero)
    }
}
