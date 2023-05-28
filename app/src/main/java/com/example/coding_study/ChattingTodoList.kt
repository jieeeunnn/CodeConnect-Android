package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.ChattingTodolistFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ua.naiksoftware.stomp.dto.StompMessage

data class TodoListItem(val todoId: Double, val content: String, var completed: Boolean)
data class TodoDelete(val result: Boolean)

class ChattingTodoList:Fragment(R.layout.chatting_todolist_fragment) {
    private val checklistItems = mutableListOf<TodoListItem>()
    private lateinit var binding: ChattingTodolistFragmentBinding
    private lateinit var adapter: ChecklistAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)



    fun onBackPressed() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ChattingTodolistFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        val sharedPreferencesRoomId = requireActivity().getSharedPreferences("MyRoomId", Context.MODE_PRIVATE)
        val roomId = sharedPreferencesRoomId?.getLong("roomId", 0) // 저장해둔 roomId 가져오기

        // RecyclerView 초기화
        val layoutManager = LinearLayoutManager(requireContext())
        binding.todoListRecyclerView.layoutManager = layoutManager

        // 채팅 프래그먼트에서 StompViewModel 인스턴스 가져오기
        val stompViewModel: StompViewModel by activityViewModels()

        // 어댑터 생성 및 RecyclerView에 설정
        adapter = roomId?.let { ChecklistAdapter(stompViewModel, it,checklistItems) }!!
        binding.todoListRecyclerView.adapter = adapter


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
        val chatRoomOnlyService = retrofitBearer.create(ChatRoomOnlyService::class.java)

        if (roomId != null) {
            chatRoomOnlyService.chatRoomOnly(roomId).enqueue(object : Callback<ChatRoomOnlyResponse> {
                override fun onResponse(
                    call: Call<ChatRoomOnlyResponse>,
                    response: Response<ChatRoomOnlyResponse>
                ) {
                    val todoListResponse = response.body()
                    val todoListMap = todoListResponse?.data as? Map<*,*>
                    val todoList = todoListMap?.get("TODO_LIST") as? List<Map<String, Any>>

                    val convertedTodoList = todoList?.mapNotNull { todo ->
                        TodoListItem (
                            todoId = todo["todoId"] as Double,
                            content = todo["content"] as String,
                            completed = todo["completed"] as Boolean
                                )
                    }

                    convertedTodoList?.forEach { todo ->
                        val todoListItem = TodoListItem(
                            todo.todoId,
                            todo.content,
                            todo.completed
                        )
                        adapter.addTodoItem(todoListItem)
                    }
                    Log.e("ChattingTodoList todoList", convertedTodoList.toString())
                }

                override fun onFailure(call: Call<ChatRoomOnlyResponse>, t: Throwable) {
                    Log.e("ChattingFragment todoList", "Failed", t)
                    Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()                }

            })
        }


        // 추가 버튼 클릭 이벤트 처리
        binding.todoListAddButton.setOnClickListener {
            childFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.chatTodoListLayout, ChattingTodoListUpload())
                .commit()
        }

        subscribeTodoList(roomId)

        return view
    }

    @SuppressLint("CheckResult")
    private fun subscribeTodoList(roomId: Long) {
        val stompViewModel: StompViewModel by activityViewModels()
        val stompClient = stompViewModel.getStompClient() // Stomp 클라이언트를 StompViewModel에서 가져옴

        if (stompClient != null) {
            if (stompClient.isConnected) {
                stompClient.topic("/sub/todo/room/$roomId")?.subscribe ({ // 메시지 구독
                        response ->

                    val body = response.payload
                    // response를 JSON 객체로 파싱

                    Log.e("ChattingTodoList delete result1", body)
                    // "result" 값이 있는지 확인
                    if (body == "true") {

                        Log.e("ChattingTodoList delete result2", body)

                    } else {
                        // "result" 값이 없는 경우 일반적인 데이터를 파싱
                        val message = parseTodoList(response)
                        val todoId = message.todoId

                        val existingItem = adapter.getItemById(todoId)

                        if (existingItem == null) {
                            coroutineScope.launch {
                                adapter.addTodoItem(message)
                            }
                        }
                    }
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

}