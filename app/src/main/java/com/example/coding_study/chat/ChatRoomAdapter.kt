package com.example.coding_study.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.ChatRoomBinding
import com.example.coding_study.study.ChatRoom

class ChatRoomAdapter(var chatList: List<ChatRoom>, private var onItemClickListener: OnItemClickListener): RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) { // 클릭 리스너를 설정하는 메서드
        this.onItemClickListener = listener
    }

    inner class ChatRoomViewHolder(private val binding: ChatRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatRoomEntity: ChatRoom) {
            binding.chatRoomTitleTextView.text = chatRoomEntity.title
            binding.chatRoomHostText.text = chatRoomEntity.hostNickname
            binding.chatRoomCountText.text = chatRoomEntity.currentCount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val binding = ChatRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatRoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(chatList[position])
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int = chatList.size

}