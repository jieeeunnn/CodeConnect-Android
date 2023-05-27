package com.example.coding_study

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.ChattingTodolistItemBinding
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp

class ChecklistAdapter(private val roomId: Long, private val items: MutableList<TodoListItem>) : RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    fun addTodoItem(todoItem: TodoListItem) {
        items.add(todoItem)
        notifyItemInserted(items.size - 1)
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

                val data = JSONObject()
                data.put("todoId", item.todoId)
                data.put("completed", item.completed)
                data.put("content", item.content)
                data.put("roomId", roomId.toString())

                 val stompClient = Stomp.over(
                    Stomp.ConnectionProvider.OKHTTP,
                    "ws://112.154.249.74:8080/ws"
                )

                // WebSocket을 통해 체크박스 클릭 시 서버에 정보 전송
                stompClient?.send("/pub/todo/update", data.toString())?.subscribe()
                Log.e("CheckListAdater", data.toString())
            }

        }

    }
}
