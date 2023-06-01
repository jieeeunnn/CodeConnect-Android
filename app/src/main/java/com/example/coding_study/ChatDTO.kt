package com.example.coding_study

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

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

data class ChatRoomServer(
    var chatId: Int,
    var nickname: String,
    var message: String,
    var currentDateTime: String,
    var profileImagePath: String,
    val messageType: String
        )



interface ChatRoomDeleteService {
    @DELETE("/chatRoom/{id}")
    fun deleteChatRoom(@Path("id") roomId: Long): Call<Void>
}



interface ChatFileUploadService { // 채팅방에서 파일 업로드
    @Multipart
    @POST("/chat/file/upload")
    fun uploadFile(
        @Part file: MultipartBody.Part
    ): Call<FileUploadResponse>
}

data class FileUploadResponse(
    var result: Boolean,
    var message: String,
    var data: FileData
)

data class FileData (
    var filePath: String,
    var fileSize: String,
    var fileContentType: String
        )



interface ChatFileDownloadService {
    @GET("/chat/file/download")
    fun fileDownload(
        @Query("filePath") filePath: String,
        @Query("fileContentType") fileContentType: String
    ): Call<Void>
}
