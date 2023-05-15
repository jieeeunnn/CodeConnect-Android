package com.example.coding_study

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.ChatFragmentBinding

class ChatFragment : Fragment(R.layout.chat_fragment) {
    private lateinit var chatAdapter: ChatRoomAdapter
    private lateinit var binding: ChatFragmentBinding
    private lateinit var onItemClickListener: ChatRoomAdapter.OnItemClickListener
    private lateinit var chatRoomViewModel: ChatRoomViewModel


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChatFragmentBinding.inflate(inflater, container, false)
        val chatRecyclerView = binding.chatRecyclerView

        var onItemClickListener: ChatRoomAdapter.OnItemClickListener = object : ChatRoomAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                TODO("Not yet implemented")
            }
        }

        chatAdapter = ChatRoomAdapter(listOf(), onItemClickListener)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)

        chatRoomViewModel = ViewModelProvider(requireActivity(), ChatRoomViewModelFactory(ChatRoomDatabase.getInstance(requireContext()).chatRoomDao())).get(ChatRoomViewModel::class.java)

        //chatRoomViewModel = ViewModelProvider(requireActivity()).get(ChatRoomViewModel::class.java)
        chatRoomViewModel.chatRooms.observe(viewLifecycleOwner) { chatRooms ->
            chatAdapter.submitList(chatRooms)
        }

        chatRecyclerView.adapter = chatAdapter

        return binding.root
    }
}
