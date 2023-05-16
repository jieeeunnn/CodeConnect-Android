package com.example.coding_study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.ChatMessageBinding

data class ChatMessage (
    var message : String
        )

class ChattingAdapter(private var chatMessages: MutableList<ChatMessage>): RecyclerView.Adapter<ChattingAdapter.ChatMessageViewHolder>() {

    inner class ChatMessageViewHolder(private val binding: ChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage) {
            binding.chatMessageTextView.text = chatMessage.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val binding = ChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatMessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(chatMessages[position])

    }

    fun addMessage(message: ChatMessage) {
        chatMessages.add(message)
        notifyItemInserted(chatMessages.size - 1)
    }

    override fun getItemCount(): Int = chatMessages.size
}