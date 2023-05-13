package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.ChatFragmentBinding

class ChatFragment : Fragment(R.layout.chat_fragment), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var chatAdapter: ChatRoomAdapter
    private lateinit var binding: ChatFragmentBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var onItemClickListener: ChatRoomAdapter.OnItemClickListener

    private fun loadSavedStudy(context: Context): ChatRoom? {
        val sharedPreferences = context.getSharedPreferences("MyStudy", Context.MODE_PRIVATE)
        val roomId = sharedPreferences.getLong("study_roomId", -1)
        val title = sharedPreferences.getString("study_title", null)
        val hostNickname = sharedPreferences.getString("study_hostNickname", null)
        val currentDateTime = sharedPreferences.getString("study_currentDateTime", null)
        val currentCount = sharedPreferences.getInt("study_currentCount", 0)

        return if (roomId != -1L && title != null && hostNickname != null && currentDateTime != null) {
            ChatRoom(roomId, title, hostNickname, currentDateTime, currentCount)
        } else {
            null
        }
    }

    override fun onStart() {
        super.onStart()
        // SharedPreferences 리스너 등록
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        // SharedPreferences 리스너 등록 해제
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1 == "MyStudy") {
            // 채팅방 정보가 변경됐을 때 처리할 로직 구현
            val chatRoom = context?.let { loadSavedStudy(it) } ?: return
            chatRoom.let { createChatRoom(it) }

            chatAdapter.notifyDataSetChanged()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chat_fragment, container, false)
        binding = ChatFragmentBinding.bind(view)
        val chatRecyclerView = binding.chatRecyclerView

        sharedPreferences = requireContext().getSharedPreferences("MyStudy", Context.MODE_PRIVATE)
        val chatRoom = loadSavedStudy(requireContext())
        chatRoom?.let { createChatRoom(it) }

        var onItemClickListener: ChatRoomAdapter.OnItemClickListener = object : ChatRoomAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                TODO("Not yet implemented")
            }
        }
        chatAdapter = ChatRoomAdapter(listOf(), onItemClickListener)
        chatRecyclerView.adapter = chatAdapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun createChatRoom(chatRoom: ChatRoom) {

        chatAdapter = ChatRoomAdapter(listOf(chatRoom), onItemClickListener)
        binding.chatRecyclerView.adapter = chatAdapter
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)
    }

}