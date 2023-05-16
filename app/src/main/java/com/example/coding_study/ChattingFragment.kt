package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
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
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompCommand
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.*


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

        connectToChatServer()

        val sharedPreferences = requireActivity().getSharedPreferences("MyRoomId", Context.MODE_PRIVATE)
        val roomId = sharedPreferences?.getLong("roomId", 0) // 저장해둔 토큰값 가져오기

        val sharedPreferences2 = requireActivity().getSharedPreferences("MyNickname", Context.MODE_PRIVATE)
        val nickname = sharedPreferences2?.getString("nickname", "") // 저장해둔 토큰값 가져오기


        binding.chatButton.setOnClickListener {
            val message = binding.chatEditText.text.toString()
            if (roomId != null) {
                if (nickname != null) {
                    sendMessage(message, roomId, nickname)
                }
            }
            binding.chatEditText.setText("")
        }
        return binding.root
    }


    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        disconnectFromChatServer()
    }

    private fun connectToChatServer() { // webSocket 연결 생성
        coroutineScope.launch(Dispatchers.IO) {
            // Stomp 클라이언트 생성
            stompClient = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                "ws://112.154.249.74:8080/ws"
            )

            stompClient?.connect()
            Log.e("ChattingFragment connectToChatServer", " ")

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
        stompClient?.topic("/sub/chat/room/1")?.subscribe { // 메시지 구독
            val message = parseMessage(it) // 전송된 stomp 메시지를 ChatMessage 객체로 파싱

            coroutineScope.launch {
                chattingAdapter.addMessage(message) // 객체를 채팅 어댑터에 추가
                binding.chattingRecyclerView.smoothScrollToPosition(chattingAdapter.itemCount - 1) // 채팅메시지가 표시되는 위치로 스크롤 이동
            }
        }
    }

    private fun sendMessage(message: String, roomId: Long, nickname: String) {

        val headers = listOf(
            StompHeader(StompHeader.DESTINATION, "/pub/chat/message"),
            StompHeader(StompHeader.CONTENT_TYPE, "application/json"),
            StompHeader("roomId", roomId.toString()),
            StompHeader("nickname", nickname),
            StompHeader(StompHeader.ID, UUID.randomUUID().toString()),
        )
        stompClient?.send(StompMessage(StompCommand.SEND, headers, message))

        /*
        val headers = mapOf(
            "roomId" to roomId, // 보내고자 하는 방의 id
            "nickname" to nickname // 메시지를 보내는 사용자의 닉네임
        )
        stompClient?.send("/pub/chat/message", message, headers)
         */
        //stompClient?.send("/pub/chat/message", message)
        Log.e("ChattingFragment sendMessage", "$message, $roomId, $nickname")

        val chatMessage = ChatMessage(message, "me")

        coroutineScope.launch {
            chattingAdapter.addMessage(chatMessage) // 객체를 채팅 어댑터에 추가
            binding.chattingRecyclerView.smoothScrollToPosition(chattingAdapter.itemCount - 1) // 채팅메시지가 표시되는 위치로 스크롤 이동
        }

    }

    private fun parseMessage(topicMessage: StompMessage): ChatMessage {
        // 파싱 로직 구현
        val body = topicMessage.payload // payload를 이용하여 메시지 내용 추출
        val messageJson = JSONObject(body)
        val message = messageJson.getString("message")
        //val timestamp = messageJson.getLong("timestamp")
        //val sender = messageJson.getString("sender")
        Log.e("ChattingFragment parseMessage", message)

        return ChatMessage(message, "")
    }
}