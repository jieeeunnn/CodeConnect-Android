package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.ChattingFragmentBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.*


@Suppress("DEPRECATION")
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

        val sharedPreferences3 = requireActivity().getSharedPreferences("MyTitle", Context.MODE_PRIVATE)
        val roomTitle = sharedPreferences3?.getString("title", "")

        binding.chattingTitleTextView.text = roomTitle

        val sharedPreferences = requireActivity().getSharedPreferences("MyRoomId", Context.MODE_PRIVATE)
        val roomId = sharedPreferences?.getLong("roomId", 0) // 저장해둔 토큰값 가져오기

        val sharedPreferences2 = requireActivity().getSharedPreferences("MyNickname", Context.MODE_PRIVATE)
        val nickname = sharedPreferences2?.getString("nickname", "") // 저장해둔 토큰값 가져오기

        // 저장해놓은 채팅 메세지 가져와서 띄우기
        val bundle = arguments
        Log.e("chattingFragment bundle ", bundle.toString())

        /*
        if (bundle != null) {
            val chattingListJson = bundle.getString("chatList")
            if (!chattingListJson.isNullOrEmpty()) {
                val chatMessages = gson.fromJson(chattingListJson, Array<ChatRoomServer>::class.java).toList()
                chatMessages.forEach { message ->
                    val sender = if (message.nickname == nickname) "me" else ""
                    val chatMessage = ChatMessage(message.message, sender, message.nickname, message.currentDateTime)
                    chattingAdapter.addMessage(chatMessage)
                }
            }
        }
        */

        // ChattingFragment에서 번들을 받아올 때 타입을 맞춰줍니다.
        if (bundle != null) {
            val chatList = bundle.getSerializable("chatList") as? ArrayList<ChatMessage>
            Log.e("chattingFragment chatList*******", chatList.toString())
            if (chatList != null) {
                for (message in chatList) {
                    val sender = if (message.nickname == nickname) "me" else ""
                    val chatMessage = ChatMessage(message.message, sender, message.nickname, message.currentCount)
                    chattingAdapter.addMessage(chatMessage)
                }
            }
        }

        connectToChatServer()

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

            val sharedPreferences = requireActivity().getSharedPreferences("MyRoomId", Context.MODE_PRIVATE)
            val roomId = sharedPreferences?.getLong("roomId", 0) // 저장해둔 토큰값 가져오기

            if (roomId != null) {
                subscribeToChatTopic(roomId)
            }
            /*
            // 채팅방 입장 요청
            val enterDestination = "/pub/chat/enter"
            val enterPayload = JSONObject().apply {
                put("roomId", roomId.toString())
            }
            stompClient?.send(enterDestination, enterPayload.toString())

             */

        }
    }

    private fun disconnectFromChatServer() { // WebSocket 연결을 종료
        coroutineScope.launch(Dispatchers.IO) {
            stompClient?.disconnect()
        }
    }

    @SuppressLint("CheckResult")
    private fun subscribeToChatTopic(roomId: Long) {
        stompClient?.topic("/sub/chat/room/$roomId")?.subscribe ({ // 메시지 구독
                response ->
            val message = parseMessage(response) // 전송된 stomp 메시지를 ChatMessage 객체로 파싱
            // 자신이 보낸 메시지인지 확인
            val sharedPreferences2 = requireActivity().getSharedPreferences("MyNickname", Context.MODE_PRIVATE)
            val nickname = sharedPreferences2?.getString("nickname", "") // 저장해둔 닉네임 가져오기

            if (nickname != null && message.nickname != nickname) {
                Log.e("chatting message ", "nickname: $nickname")
                coroutineScope.launch {
                    chattingAdapter.addMessage(message) // 객체를 채팅 어댑터에 추가
                    binding.chattingRecyclerView.smoothScrollToPosition(chattingAdapter.itemCount - 1) // 채팅메시지가 표시되는 위치로 스크롤 이동
                }
            }
        },
            { error ->
                // 예외 처리
                error.printStackTrace()
            }
        )
    }

    private fun sendMessage(message: String, roomId: Long, nickname: String) {

        val data = JSONObject()
        data.put("roomId", roomId.toString())
        data.put("nickname", nickname)
        data.put("message", message)
        val currentDateTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val chatMessage = ChatMessage(message, "me", nickname, currentDateTime)

        stompClient?.send("/pub/chat/message", data.toString())?.subscribe()

        Log.e("ChattingFragment sendMessage", "$message, $roomId, $nickname")

        coroutineScope.launch {
            chattingAdapter.addMessage(chatMessage) // 객체를 채팅 어댑터에 추가
            binding.chattingRecyclerView.smoothScrollToPosition(chattingAdapter.itemCount - 1) // 채팅메시지가 표시되는 위치로 스크롤 이동
        }
    }

    private fun parseMessage(topicMessage: StompMessage): ChatMessage {
        // 파싱 로직 구현
        val body = topicMessage.payload // payload를 이용하여 메시지 내용 추출
        val messageJson = JSONObject(body)
        val nicknameJson = JSONObject(body)
        val currentDateTimeJson = JSONObject(body)
        val message = messageJson.getString("message")
        val nickname = nicknameJson.getString("nickname")
        Log.e("ChattingFragment parseMessage", "$message, $nickname")

        val currentDateTime = currentDateTimeJson.getString("currentDateTime")

        Log.e("ChattingFragment parseMessage", "$message, $nickname, $currentDateTime")

        return ChatMessage(message, "", nickname, currentDateTime)
    }
}