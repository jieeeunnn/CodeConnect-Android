package com.example.coding_study

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.ChattingFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompMessage

class ChattingFragment: Fragment(R.layout.chatting_fragment) {
    private lateinit var chattingAdapter: ChattingAdapter
    private lateinit var binding: ChattingFragmentBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var stompClient: StompClient? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChattingFragmentBinding.inflate(inflater, container, false)
        val chattingRecyclerView = binding.chattingRecyclerView

        chattingAdapter = ChattingAdapter(mutableListOf())
        binding.chattingRecyclerView.layoutManager = LinearLayoutManager(context)
        chattingRecyclerView.adapter = chattingAdapter

        binding.chatButton.setOnClickListener {
            val message = binding.chatEditText.text.toString()
            //sendMessage(message)
            binding.chatEditText.setText("")
        }

        //connectToChatServer()

        return binding.root
    }

    /*
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        disconnectFromChatServer()
    }

    private fun connectToChatServer() { // webSocket 연결 생성
        coroutineScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder().url("ws://112.154.249.74:8080/ws").build()
            //val listener = WebSocketListenerImpl()
            //val webSocket = client.newWebSocket(request, listener)
            val intervalMillis = 1000L

            stompClient = Stomp.over(
                request, intervalMillis, client
            )

            stompClient?.connect()
            subscribeToChatTopic()
        }
    }

    private fun disconnectFromChatServer() { // WebSocket 연결을 종료
        coroutineScope.launch(Dispatchers.IO) {
            stompClient?.disconnect()
        }
    }

    @SuppressLint("CheckResult")
    private fun subscribeToChatTopic() {
        stompClient?.topic("/sub/chat/room/1")?.subscribe {
            val message = parseMessage(it)
            coroutineScope.launch {
                chattingAdapter.addMessage(message)
                binding.chattingRecyclerView.smoothScrollToPosition(chattingAdapter.itemCount - 1)
            }
        }
    }

    private fun sendMessage(message: String) {
        stompClient?.send("/pub/chat/message", message)
    }

    private fun parseMessage(topicMessage: StompMessage): ChatMessage {
        // 파싱 로직 구현
    }

     */
}