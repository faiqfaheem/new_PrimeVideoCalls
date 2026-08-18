package com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity
import com.axis.vpn.tools.prankvideocall.databinding.ItemCustomHeroBinding
import com.bumptech.glide.Glide
import java.io.File
class CustomAdapter(
    private val onClick: (CustomCallerEntity) -> Unit,
    private val onDelete: (CustomCallerEntity) -> Unit,
    private val onAddContactClick: () -> Unit
) : ListAdapter<CustomCallerItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_CUSTOM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_ADD else TYPE_CUSTOM
    }

    inner class AddViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class CustomViewHolder(
        val binding: ItemCustomHeroBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ADD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_contact, parent, false)
            AddViewHolder(view)
        } else {
            val binding = ItemCustomHeroBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            CustomViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is AddViewHolder -> {
                holder.itemView.setOnClickListener {
                    onAddContactClick()
                }
            }

            is CustomViewHolder -> {
                val item = getItem(position) as? CustomCallerItem.CallerItem
                item?.let { callerItem ->
                    val caller = callerItem.caller

                    holder.binding.apply {
                        // Load from file path, not URI
                        if (caller.imagePath.isNotEmpty() && File(caller.imagePath).exists()) {
                            Glide.with(root.context)
                                .load(File(caller.imagePath))  // Use File instead of Uri
                                .centerCrop()
                                .into(ivHero)
                        } else {
                            // Fallback or placeholder
                            ivHero.setImageResource(R.drawable.hero_one)
                        }

                        tvName.text = caller.name

                        root.setOnClickListener {
                            onClick(caller)
                        }

                        btnDelete.setOnClickListener {
                            onDelete(caller)
                        }
                    }
                }
            }
        }
    }

    // DiffUtil callback for efficient updates
    class DiffCallback : DiffUtil.ItemCallback<CustomCallerItem>() {
        override fun areItemsTheSame(
            oldItem: CustomCallerItem,
            newItem: CustomCallerItem
        ): Boolean {
            return when {
                oldItem is CustomCallerItem.AddButton && newItem is CustomCallerItem.AddButton -> true
                oldItem is CustomCallerItem.CallerItem && newItem is CustomCallerItem.CallerItem ->
                    oldItem.caller.id == newItem.caller.id
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: CustomCallerItem,
            newItem: CustomCallerItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}

// Sealed class to handle both add button and caller items
sealed class CustomCallerItem {
    object AddButton : CustomCallerItem()
    data class CallerItem(val caller: CustomCallerEntity) : CustomCallerItem()
}