package com.example.coding_study

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ChatRoomGetService { // 채팅방 전체 리스트
    @GET("/chatRoom/list")
    fun chatRoomGetList(
    ): Call<ChatRoomListResponse>
}

data class ChatRoomListResponse (
    var result: Boolean,
    var message: String,
    var data: List<ChatRoom>
    )



interface ChatRoomOnlyService { // 채팅방 하나 클릭시
    @GET("/chatRoom/{id}")
    fun chatRoomOnly(
        @Path("id") roomId: Long
    ): Call<ChatRoomOnlyResponse>
}

data class ChatRoomOnlyResponse (
    val result: Boolean,
    var message: String,
    var data: Any
    )

data class MY_NICKNAME (
    var nickname: String
        )

data class CHAT (
    var data: List<ChatRoomServer>
        )

data class ChatRoomServer (
    var chatId: Int,
    var nickname: String,
    var message: String,
    var currentDateTime: String
        )



interface ChatRoomDeleteService {
    @DELETE("/chatRoom/{id}")
    fun deleteChatRoom(@Path("id") roomId: Long): Call<Void>
}