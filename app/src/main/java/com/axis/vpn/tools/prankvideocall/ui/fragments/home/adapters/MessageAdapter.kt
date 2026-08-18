package com.axis.vpn.tools.prankvideocall.ui.fragments.home.adapters


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.axis.vpn.tools.prankvideocall.R
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.Message
import com.bumptech.glide.Glide

class MessageAdapter(
    private val messages: List<Message>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val SENT_TEXT = 1
        private const val RECEIVED_TEXT = 2
        private const val SENT_IMAGE = 3
        private const val RECEIVED_IMAGE = 4
        private const val TYPING = 5
    }

    override fun getItemViewType(position: Int): Int {

        val item = messages[position]

        return when {

            item.isLoading -> TYPING

            item.imageUri != null && item.isSent ->
                SENT_IMAGE

            item.imageUri != null && !item.isSent ->
                RECEIVED_IMAGE

            item.isSent ->
                SENT_TEXT

            else ->
                RECEIVED_TEXT
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            SENT_TEXT -> TextViewHolder(
                inflater.inflate(
                    R.layout.item_message_sent,
                    parent,
                    false
                )
            )

            RECEIVED_TEXT -> TextViewHolder(
                inflater.inflate(
                    R.layout.item_message_received,
                    parent,
                    false
                )
            )

            SENT_IMAGE -> ImageViewHolder(
                inflater.inflate(
                    R.layout.item_image_sent,
                    parent,
                    false
                )
            )

            RECEIVED_IMAGE -> ImageViewHolder(
                inflater.inflate(
                    R.layout.item_image_received,
                    parent,
                    false
                )
            )

            else -> TypingViewHolder(
                inflater.inflate(
                    R.layout.item_message_typing,
                    parent,
                    false
                )
            )
        }
    }
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val message = messages[position]

        when (holder) {

            is TextViewHolder -> {
                holder.bind(message)

                // Show actions only for received text messages
                message.showActions = !message.isSent && !message.isLoading
            }

            is ImageViewHolder -> {
                holder.bind(message)

                // Show actions only for received image messages
                message.showActions = !message.isSent
            }

            is TypingViewHolder -> Unit
        }
    }
    override fun getItemCount() = messages.size
    class TypingViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvMessage =
            itemView.findViewById<TextView>(R.id.tvMessage)

        fun bind() {

            tvMessage.text = "● ● ●"
        }
    }
    class TextViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)

        private val layoutActions =
            itemView.findViewById<LinearLayout?>(R.id.layoutActions)

        private val btnCopy =
            itemView.findViewById<ImageView?>(R.id.btnCopy)

        private val btnShare =
            itemView.findViewById<ImageView?>(R.id.btnShare)

        private val btnReport =
            itemView.findViewById<ImageView?>(R.id.btnReport)

        fun bind(
            message: Message
        ) {

            tvMessage.text =
                if (message.isLoading) "Typing..." else message.text

            tvTime.text = message.timestamp




            btnCopy?.setOnClickListener {
                val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(
                    ClipData.newPlainText("message", message.text)
                )
                Toast.makeText(itemView.context, "Copied", Toast.LENGTH_SHORT).show()
            }

            btnShare?.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message.text)
                }
                itemView.context.startActivity(Intent.createChooser(intent, "Share"))
            }

            btnReport?.setOnClickListener {
                Toast.makeText(itemView.context, "Reported", Toast.LENGTH_SHORT).show()
            }
        }
    }
    class ImageViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val image = itemView.findViewById<ImageView>(R.id.ivImage)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)

        private val layoutActions =
            itemView.findViewById<LinearLayout>(R.id.layoutActions)

        fun bind(
            message: Message
        ) {

            tvTime.text = message.timestamp

            Glide.with(itemView.context)
                .load(message.imageUri)
                .centerCrop()
                .into(image)




        }
    }
}