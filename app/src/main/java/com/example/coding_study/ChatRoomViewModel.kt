package com.example.coding_study

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomViewModelFactory(private val chatRoomDao: ChatRoomDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatRoomViewModel(chatRoomDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ChatRoomViewModel(private val chatRoomDao: ChatRoomDao) : ViewModel() {

    fun saveChatRoom(chatRoom: ChatRoom) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val chatRoomEntity = ChatRoomEntity(
                    roomId = chatRoom.roomId,
                    title = chatRoom.title,
                    hostNickname = chatRoom.hostNickname,
                    currentDateTime = chatRoom.currentDateTime,
                    currentCount = chatRoom.currentCount
                )
                chatRoomDao.insertChatRoom(chatRoomEntity)
                Log.e("ChatRoomViewModel setChatRoom", "$chatRoomEntity")
            }
        }
    }

    /*
    // 데이터를 가져오는 함수
    fun getAllChatRooms(): LiveData<List<ChatRoomEntity>> {
        Log.e("ChatRoomViewModel getAllChatRooms", "$chatRoomDao")
        return chatRoomDao.getAllChatRooms()
    }

     */

    val chatRooms: LiveData<List<ChatRoomEntity>> = chatRoomDao.getAllChatRooms()
}


