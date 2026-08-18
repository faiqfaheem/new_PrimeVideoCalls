package com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.databinding.ItemCategoryBinding
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.CategoryItem
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.CategoryResponse
import com.google.android.material.card.MaterialCardView

class CategoryAdapter(
    private var list: MutableList<CategoryResponse>,
    private val onClick: (CategoryResponse) -> Unit
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val cardColors = listOf(
        Color.parseColor("#FFFFFFFF"),
        Color.parseColor("#FFFFFFFF"),
        Color.parseColor("#FFFFFFFF"),
        Color.parseColor("#FFFFFFFF")
    )
    inner class ViewHolder(
        val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(

            ItemCategoryBinding.inflate(
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

        holder.binding.tvTitle.text = item.name

        val defaultColor = cardColors[position % cardColors.size]

        if (item.isSelected) {

            holder.binding.root.setCardBackgroundColor(
                Color.parseColor("#4F46E5")
            )

            holder.binding.tvTitle.setTextColor(Color.WHITE)

        } else {

            holder.binding.root.setCardBackgroundColor(defaultColor)

            holder.binding.tvTitle.setTextColor(Color.BLACK)
        }

        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }

    fun updateSelection(id: Int) {

        list.forEach {

            it.isSelected = it.id == id

        }

        notifyDataSetChanged()

    }


}