package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.ChatFragmentBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatFragment : Fragment(R.layout.chat_fragment) {
    private lateinit var chatAdapter: ChatRoomAdapter
    private lateinit var binding: ChatFragmentBinding
    private lateinit var onItemClickListener: ChatRoomAdapter.OnItemClickListener
    private lateinit var chatRoomViewModel: ChatRoomViewModel
    private var chatList: List<ChatRoom> = emptyList() // Declare chatList as a member variable


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChatFragmentBinding.inflate(inflater, container, false)
        val chatRecyclerView = binding.chatRecyclerView


        val sharedPreferences = requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token.orEmpty())
                            .build()
                        Log.d("TokenInterceptor_StudyFragment", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val chatRoomService = retrofitBearer.create(ChatRoomGetService::class.java)
        chatRoomService.chatRoomGetList().enqueue(object : Callback<ChatRoomListResponse>{
            override fun onResponse(call: Call<ChatRoomListResponse>, response: Response<ChatRoomListResponse>
            ) {
                if (response.isSuccessful) {
                    val chatRoomListResponse = response.body()
                    Log.e("ChatFragment chatRoomGetList body", "$chatRoomListResponse")
                    Log.e("ChatFragment chatRoomGetList code", "${response.code()}")

                    val chatRoomList = chatRoomListResponse?.data
                    chatList = chatRoomList?.map {
                        ChatRoom(it.roomId, it.title, it.hostNickname, it.currentDateTime, it.currentCount)
                    } ?: emptyList()


                    if (chatRoomListResponse?.result == true) {
                        chatAdapter.chatList = chatList
                        chatAdapter.notifyDataSetChanged()
                    }
                }
            }
            override fun onFailure(call: Call<ChatRoomListResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })

        val chatRoomOnlyService = retrofitBearer.create(ChatRoomOnlyService::class.java)

        var onItemClickListener: ChatRoomAdapter.OnItemClickListener = object : ChatRoomAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {

                val roomId = chatList[position].roomId
                Log.e("ChatFragment onItemClickListener roomId", "$roomId")

                chatRoomOnlyService.chatRoomOnly(roomId).enqueue(object : Callback<ChatRoomOnlyResponse>{
                    override fun onResponse(call: Call<ChatRoomOnlyResponse>, response: Response<ChatRoomOnlyResponse>
                    ) {
                        val chattingFragment = ChattingFragment()
                        childFragmentManager.beginTransaction()
                            .replace(R.id.chat_fragment_layout, chattingFragment)
                            .addToBackStack(null)
                            .commit()
                    }

                    override fun onFailure(call: Call<ChatRoomOnlyResponse>, t: Throwable) {
                        Log.e("ChatFragment onClickListener", "Failed", t)
                        Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                    }

                })
            }
        }

        /*
        chatRoomViewModel = ViewModelProvider(requireActivity(), ChatRoomViewModelFactory(ChatRoomDatabase.getInstance(requireContext()).chatRoomDao())).get(ChatRoomViewModel::class.java)

        //chatAdapter = ChatRoomAdapter(listOf(), onItemClickListener)
        chatAdapter = ChatRoomAdapter(emptyList(), onItemClickListener)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)

        chatRoomViewModel.chatRooms.observe(viewLifecycleOwner) { chatRooms ->
            chatAdapter.submitList(chatRooms)
        }

        chatRecyclerView.adapter = chatAdapter
         */

        chatAdapter = ChatRoomAdapter(listOf(), onItemClickListener)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(context)
        chatRecyclerView.adapter = chatAdapter


        return binding.root
    }
}
