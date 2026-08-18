package com.axis.vpn.tools.prankvideocall.ui.fragments.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.databinding.RowLanguageBinding

class LanguageAdapter(
    private val onItemClick: (LanguageModel) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageVH>() {

    private val list = mutableListOf<LanguageModel>()

    fun submitList(newList: List<LanguageModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class LanguageVH(val binding: RowLanguageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageVH {
        val binding = RowLanguageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LanguageVH(binding)
    }

    override fun onBindViewHolder(holder: LanguageVH, position: Int) {

        val item = list[position]

        holder.binding.tvLanguageTitle.text = item.title

        val iconRes = when (item.title.lowercase()) {
            "arabic" -> R.drawable.ic_arabic
            "english" -> R.drawable.ic_language_default
            "french" -> R.drawable.ic_french
            "german" -> R.drawable.ic_german
            "portuguese" -> R.drawable.ic_portugal
            "russian" -> R.drawable.ic_russia
            "spanish" -> R.drawable.ic_spain
            else -> R.drawable.ic_language_default
        }

        holder.binding.tvIcon.setImageResource(iconRes)

        if (item.isSelected) {

            holder.binding.cardRoot.setBackgroundResource(R.drawable.ic_selected)

            holder.binding.tvLanguageTitle.setTextColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.white)
            )

            holder.binding.radioLanguage.isChecked = true

        } else {

            holder.binding.cardRoot.setBackgroundResource(R.drawable.ic_unselected)

            holder.binding.tvLanguageTitle.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.textGray)
            )

            holder.binding.radioLanguage.isChecked = false
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = list.size
}