package com.example.coding_study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.ChatMessageBinding
import com.example.coding_study.databinding.ChatMessageReceiveBinding

data class ChatMessage (
    var message : String,
    var sender: String,
    var nickname: String,
    var currentDateTime: String
        )

class ChattingAdapter(private var chatMessages: MutableList<ChatMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MY_MESSAGE = 0
        private const val TYPE_OTHER_MESSAGE = 1
    }

    override fun getItemViewType(position: Int): Int {
        val message = chatMessages[position]
        return if (message.sender == "me") {
            TYPE_MY_MESSAGE
        } else {
            TYPE_OTHER_MESSAGE
        }
    }

    inner class ChatMessageViewHolder(private val binding: ChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage) {
            binding.chatMessageTextView.text = chatMessage.message
            binding.myMessageNickname.text = chatMessage.nickname
            binding.myMessageCurrentTime.text = chatMessage.currentDateTime
        }
    }

    inner class OtherChatMessageViewHolder(private val binding: ChatMessageReceiveBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage) {
            binding.chatReceiveMessage.text = chatMessage.message
            binding.otherMessageNickname.text = chatMessage.nickname
            binding.otherMessageCurrentTime.text = chatMessage.currentDateTime
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MY_MESSAGE -> {
                val binding = ChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChatMessageViewHolder(binding)
            }
            TYPE_OTHER_MESSAGE -> {
                val binding = ChatMessageReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                OtherChatMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        when(holder) {
            is ChatMessageViewHolder -> holder.bind(chatMessage)
            is OtherChatMessageViewHolder -> holder.bind(chatMessage)
        }
    }

    fun addMessage(message: ChatMessage) {
        chatMessages.add(message)
        notifyItemInserted(chatMessages.size - 1)
    }

    override fun getItemCount(): Int = chatMessages.size
}