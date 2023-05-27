package com.example.coding_study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.ChattingUploadTodolistBinding
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp

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
            val content = binding.todoListUploadText.text

            val data = JSONObject()
            data.put("roomId", roomId.toString())
            data.put("content", content)

            val stompClient = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                "ws://112.154.249.74:8080/ws"
            )

            // 새로운 투두리스트 추가 시 서버에 전송
            stompClient?.send("pub/todo/create", data.toString())?.subscribe()
            Log.e("ChattingTodoListUpload new todoList", data.toString())

            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStackImmediate()
        }

        return binding.root
    }
}