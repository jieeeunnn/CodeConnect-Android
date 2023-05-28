package com.example.coding_study

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.ChattingTodolistItemBinding
import org.json.JSONObject
import ua.naiksoftware.stomp.dto.StompMessage

class ChecklistAdapter(private val stompViewModel: StompViewModel, private val roomId: Long, private val items: MutableList<TodoListItem>) : RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    fun addTodoItem(todoItem: TodoListItem) {
        items.add(todoItem)
        notifyItemInserted(items.size - 1)
    }
    // 아이템 ID를 기반으로 어댑터에서 아이템 찾아 반환하는 메서드
    fun getItemById(todoId: Double): TodoListItem? {
        return items.find { it.todoId == todoId }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ChattingTodolistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(private val binding: ChattingTodolistItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TodoListItem) {
            binding.todoListItemTextView.text = item.content
            binding.checkBox.isChecked = item.completed
            binding.checkBox.text = ""

            // 체크박스 클릭 이벤트 리스너 등록
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                item.completed = isChecked

                sendCheckBox(item.todoId.toLong(), roomId, item.content, item.completed)

                subscribeTodoList(roomId)
            }

        }

        private fun sendCheckBox(todoId: Long, roomId: Long, content: String, isCompleted: Boolean) {
            val data = JSONObject()
            data.put("todoId", todoId)
            data.put("isCompleted", isCompleted)
            data.put("content", content)
            data.put("roomId", roomId.toString())

            val stompClient = stompViewModel.getStompClient()

            if (stompClient != null) {
                Log.e("checkListAdapter checkbox stomp", "${stompClient.isConnected}")

                // WebSocket을 통해 체크박스 클릭 시 서버에 정보 전송
                stompClient?.send("/pub/todo/update", data.toString())?.subscribe()
                Log.e("CheckListAdater", data.toString())
            }
        }

        @SuppressLint("CheckResult")
        private fun subscribeTodoList(roomId: Long) {
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

            return TodoListItem(todoId, content, completed)
        }
    }
}
