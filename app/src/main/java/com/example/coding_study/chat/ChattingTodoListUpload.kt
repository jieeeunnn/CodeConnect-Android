package com.example.coding_study.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.coding_study.R
import com.example.coding_study.databinding.ChattingUploadTodolistBinding
import org.json.JSONObject
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.*

class ChattingTodoListUpload: Fragment(R.layout.chatting_upload_todolist) {
    private lateinit var binding: ChattingUploadTodolistBinding

    fun onBackPressed() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChattingUploadTodolistBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        val sharedPreferencesRoomId = requireActivity().getSharedPreferences("MyRoomId", Context.MODE_PRIVATE)
        val roomId = sharedPreferencesRoomId?.getLong("roomId", 0) // 저장해둔 roomId 가져오기

        binding.todoListUploadButton.setOnClickListener {
            val content = binding.todoListUploadText.text.toString()

            if (roomId != null) {
                sendMessage(roomId, content)
            }

            onBackPressed()

            if (roomId != null) {
                subscribeTodoList(roomId)
            }
        }

        return binding.root
    }

        @SuppressLint("CheckResult")
        private fun subscribeTodoList(roomId: Long) {
        val stompViewModel: StompViewModel by activityViewModels()

    // Stomp 클라이언트를 StompViewModel에서 가져옴
        val stompClient = stompViewModel.getStompClient()

        if (stompClient != null) {
            if (stompClient.isConnected) {
                stompClient?.topic("/sub/todo/room/$roomId")?.subscribe ({ // 메시지 구독
                        response ->
                    val message = parseTodoList(response) // 전송된 stomp 메시지를 ChatMessage 객체로 파싱

                },
                    { error ->
                        // 예외 처리
                        error.printStackTrace()
                    }
                )
            }
        } else
        {
            Log.e("chattingTodoListUpload stomp is","null")
        }
        }

        private fun parseTodoList(todoListItem: StompMessage): TodoListItem {
            // 파싱 로직 구현
            val body = todoListItem.payload // payload를 이용하여 메시지 내용 추출

            val todoIdJson = JSONObject(body)
            val contentJson = JSONObject(body)
            val completedJson = JSONObject(body)

            val todoId = todoIdJson.getDouble("todoId")
            val content = contentJson.getString("content")
            val completed = completedJson.getBoolean("completed")

            Log.e("Chatting todoListUpload parse todoListItem", "$todoId, $content, $completed")

            return TodoListItem(todoId, content, completed)
        }

    private fun sendMessage(roomId: Long, content: String) {

        val data = JSONObject()
        data.put("roomId", roomId.toString())
        data.put("content", content)

        // 채팅 프래그먼트에서 StompViewModel 인스턴스 가져오기
        val stompViewModel: StompViewModel by activityViewModels()

        // Stomp 클라이언트를 StompViewModel에서 가져옴
        val stompClient = stompViewModel.getStompClient()

        if (stompClient != null) {
            if (stompClient.isConnected) {
                // 새로운 투두리스트 추가 시 서버에 전송
                stompClient?.send("/pub/todo/create", data.toString())?.subscribe()
            }
        }

    }

}