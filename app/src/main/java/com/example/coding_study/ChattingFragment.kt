package com.example.coding_study

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.coding_study.databinding.ChatMemberListBinding
import com.example.coding_study.databinding.ChattingFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.*
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.*


@Suppress("DEPRECATION")
class ChattingFragment: Fragment(R.layout.chatting_fragment),  DeleteDialogInterface{
    private lateinit var chattingAdapter: ChattingAdapter
    private lateinit var binding: ChattingFragmentBinding
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var stompClient: StompClient? = null

    fun saveMyImagePath(context: Context, myImagePath: String?) {
        val sharedPreferences = context.getSharedPreferences("MyImagePath", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("imagePath", myImagePath)
        if (!editor.commit()) {
            Log.e("saveMyImagePath", "Failed to save image Path")
        }
    }

    fun saveFilePath(context: Context, myImagePath: String?) {
        val sharedPreferences = context.getSharedPreferences("MyFilePath", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("myFilePath", myImagePath)
        if (!editor.commit()) {
            Log.e("saveFilePath", "Failed to save image Path")
        }
    }
    fun saveFileContentType(context: Context, myImagePath: String?) {
        val sharedPreferences = context.getSharedPreferences("MyFileContentType", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("myFileContentType", myImagePath)
        if (!editor.commit()) {
            Log.e("saveFileContentType", "Failed to save image Path")
        }
    }


    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 123
    }

    // + 버튼 클릭 시 파일 선택기(Dialog)를 열기
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // 모든 파일 타입
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                fileName = displayName
            }
        }
        return fileName
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { fileUri ->
                // 파일을 선택한 후에 여기에서 파일을 전송하는 로직을 구현합니다.
                // 선택한 파일을 서버에 업로드하는 등의 작업을 수행할 수 있습니다.

                val inputStream = requireContext().contentResolver.openInputStream(fileUri)
                val fileName = getFileName(fileUri) // 파일 이름을 얻는 유틸리티 함수 호출

                val requestBody = inputStream?.let { RequestBody.create("multipart/form-data".toMediaTypeOrNull(), it.readBytes()) }
                val filePart = requestBody?.let {
                    MultipartBody.Part.createFormData("file", fileName, it)
                }
                if (fileName != null) {
                    Log.e("chat file upload fileName", fileName)
                }

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

                val sharedPreferencesRoomId = requireActivity().getSharedPreferences("MyRoomId", Context.MODE_PRIVATE)
                val roomId = sharedPreferencesRoomId?.getLong("roomId", 0) // 저장해둔 roomId 가져오기

                val sharedPreferences2 = requireActivity().getSharedPreferences("MyNickname", Context.MODE_PRIVATE)
                val myNickname = sharedPreferences2?.getString("nickname", "") // 저장해둔 닉네임 가져오기

                val fileUploadService = retrofitBearer.create(ChatFileUploadService::class.java)

                if (filePart != null) {
                    fileUploadService.uploadFile(filePart).enqueue(object : Callback<FileUploadResponse>{
                        override fun onResponse(
                            call: Call<FileUploadResponse>,
                            response: Response<FileUploadResponse>
                        ) {
                            if (response.isSuccessful) {
                                Log.e("Chat file upload response code", "${response.code()}")
                                Log.e("Chat file upload response body", "${response.body()}")

                                val fileResponse = response.body()
                                val filePath = fileResponse?.data?.filePath
                                val fileSize = fileResponse?.data?.fileSize
                                val fileType = fileResponse?.data?.fileContentType

                                context?.let { saveFilePath(it, filePath) }
                                context?.let { saveFileContentType(it, fileType) }

                                val message = "파일 경로: $filePath\n파일 타입: $fileType\n파일 크기: $fileSize"
                                if (roomId != null) {
                                    if (myNickname != null) {
                                        sendMessage(message, roomId, myNickname, MessageType.FILE)
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                            Log.e("ChattingFragment file upload", "Failed", t)
                            Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()                        }

                    })
                }

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChattingFragmentBinding.inflate(inflater, container, false)
        val chattingRecyclerView = binding.chattingRecyclerView
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val chatMenuButton: ImageView = binding.chatMenuButton

        // 채팅방 drawerLayout
        chatMenuButton.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.openDrawer(GravityCompat.END)
            } else {
                drawerLayout.closeDrawer(GravityCompat.END)
            }
        }

        binding.checkListTextView.setOnClickListener {
            drawerLayout.closeDrawers() // drawerLayout 자동 닫기

            childFragmentManager.beginTransaction()
                .addToBackStack(null)
                .replace(R.id.drawerLayout, ChattingTodoList()) // TodoList 보여주기
                .commit()
        }

        val sharedPreferencesRoomId = requireActivity().getSharedPreferences("MyRoomId", Context.MODE_PRIVATE)
        val roomId = sharedPreferencesRoomId?.getLong("roomId", 0) // 저장해둔 roomId 가져오기

        val sharedPreferences2 = requireActivity().getSharedPreferences("MyNickname", Context.MODE_PRIVATE)
        val myNickname = sharedPreferences2?.getString("nickname", "") // 저장해둔 닉네임 가져오기

        // 채팅방 나가기
        binding.roomDeleteTextView.setOnClickListener {
            val deleteDialog = roomId?.let { it1 -> DeleteDialog(this, it1, "채팅방에서 나가시겠습니까?") }
            if (deleteDialog != null) {
                deleteDialog.isCancelable = false
            }
            if (deleteDialog != null) {
                deleteDialog.show(this.childFragmentManager, "deleteDialog")
            }
        }


        chattingAdapter = requireActivity()?.let { ChattingAdapter(it, mutableListOf()) }!!
        binding.chattingRecyclerView.layoutManager = LinearLayoutManager(context)
        chattingRecyclerView.adapter = chattingAdapter

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
                override fun onResponse(call: Call<ChatRoomOnlyResponse>, response: Response<ChatRoomOnlyResponse>
                ) {

                    val responseChatRoom = response.body()
                    Log.e("ChattingFragment chattingList", "$responseChatRoom")

                    val roomInfoData = (responseChatRoom?.data as Map<*, *>)?.get("ROOM_INFO") as? Map<String, Any>
                    val title = roomInfoData?.get("title") as? String
                    if (title != null) {
                        Log.e("chattingFragment title", title)
                        binding.chattingTitleTextView.text = title

                    }

                    // 이전 채팅 내용 가져오기
                    val chatMap = responseChatRoom.data as? Map<*, *>
                    val chatList = chatMap?.get("CHAT") as? List<Map<String, Any>>

                    val convertedChatList = chatList?.mapNotNull { chat ->
                        (chat["chatId"] as? Double)?.let { chatId ->
                            (chat["nickname"] as? String)?.let { nickname ->
                                ChatRoomServer(
                                    chatId = chatId.toInt(),
                                    nickname = nickname,
                                    message = chat["message"] as? String ?: "",
                                    currentDateTime = chat["currentDateTime"] as? String ?: "",
                                    profileImagePath = chat["profileImagePath"] as String,
                                    messageType = chat["messageType"] as String
                                )
                            }
                        }
                    }


                    // 채팅 내용 어댑터에 띄우기
                    convertedChatList?.forEach { chat ->
                        val isMessage = if (chat.messageType == "FILE") MessageType.FILE else MessageType.CHAT
                        val sender = if (chat.nickname == myNickname) "me" else ""
                        val chatMessage = ChatMessage(
                            chat.message,
                            sender,
                            chat.nickname,
                            chat.currentDateTime,
                            chat.profileImagePath,
                            isMessage
                        )
                        if (isMessage == MessageType.CHAT) {
                            chattingAdapter.addMessage(chatMessage)
                        } else {
                            chattingAdapter.addFileMessage(chatMessage)
                        }
                    }
                    Log.e("ChattingFrgment chatList", convertedChatList.toString())

                    val nicknameImageMap = chatMap?.get("NICKNAME_IMAGE") as? Map<String, String>
                    val memberInfoMap = HashMap<String, String>()

                    if (nicknameImageMap != null) {
                        for ((nickname, imageUrl) in nicknameImageMap) {
                            memberInfoMap[nickname] = imageUrl
                        }
                    }

                    val memberListLayout = binding.membersLinearLayout

                    if (memberInfoMap.isNotEmpty()) {
                        for ((nickname, imageUrl) in memberInfoMap) {

                            if (nickname == myNickname) {
                                context?.let { saveMyImagePath(it, imageUrl) }
                            }
                            val memberView = LayoutInflater.from(context).inflate(R.layout.chat_member_list, null)
                            val binding = ChatMemberListBinding.bind(memberView)

                            val memberNicknameTextView = binding.memberNickname

                            val imageUrl: String? = "http://112.154.249.74:8080/"+ imageUrl
                            val imageView: ImageView = binding.memberProfileImage
                            val loadImageTask = LoadImageTask(imageView)
                            loadImageTask.execute(imageUrl)

                            // 프로필 사진 로딩 및 설정 (예시: Glide 라이브러리 사용)
                            context?.let {
                                Glide.with(it)
                                    .load(imageUrl)
                                    .circleCrop()
                                    .into(imageView)
                            }

                            // 닉네임 설정
                            memberNicknameTextView.text = nickname

                            // 멤버 뷰를 LinearLayout에 추가
                            memberListLayout.addView(memberView)
                        }
                    }


                }

                override fun onFailure(call: Call<ChatRoomOnlyResponse>, t: Throwable) {
                    Log.e("ChatFragment onClickListener", "Failed", t)
                    Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                }

            })
        }

        connectToChatServer()

        binding.chatButton.setOnClickListener {
            val message = binding.chatEditText.text.toString()
            if (roomId != null) {
                if (myNickname != null) {
                    sendMessage(message, roomId, myNickname, MessageType.CHAT)
                }
            }
            binding.chatEditText.setText("")
        }

        binding.fileButton.setOnClickListener {
            openFilePicker()
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

// 채팅 프래그먼트에서 StompViewModel 인스턴스 가져오기
            val stompViewModel: StompViewModel by activityViewModels()

            // Stomp 클라이언트를 StompViewModel에 저장
            stompViewModel.setStompClient(stompClient)

            val sharedPreferences = requireActivity().getSharedPreferences("MyRoomId", Context.MODE_PRIVATE)
            val roomId = sharedPreferences?.getLong("roomId", 0) // 저장해둔 토큰값 가져오기

            if (roomId != null) {
                subscribeToChatTopic(roomId)
            }
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
                    if (message.messageType == MessageType.CHAT) {
                        chattingAdapter.addMessage(message) // 객체를 채팅 어댑터에 추가
                    } else if (message.messageType == MessageType.FILE) {
                        chattingAdapter.addFileMessage(message)
                    }
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

    private fun sendMessage(message: String, roomId: Long, nickname: String, messageType: MessageType) {

        val data = JSONObject()
        data.put("roomId", roomId.toString())
        data.put("nickname", nickname)
        data.put("message", message)
        data.put("messageType", messageType)
        val currentDateTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val sharedPreferences = requireActivity().getSharedPreferences("MyImagePath", Context.MODE_PRIVATE)
        val myImagePath = sharedPreferences?.getString("imagePath", "") // 저장해둔 토큰값 가져오기

        data.put("profileImagePath", myImagePath)

        val chatMessage = myImagePath?.let { ChatMessage(message, "me", nickname, currentDateTime, myImagePath, messageType) }

        stompClient?.send("/pub/chat/message", data.toString())?.subscribe()

        Log.e("ChattingFragment sendMessage", "$message, $roomId, $nickname, $myImagePath, $messageType")

        coroutineScope.launch {

                if (messageType == MessageType.CHAT) {
                    if (chatMessage != null) {
                        chattingAdapter.addMessage(chatMessage)
                    }
                } else if(messageType == MessageType.FILE){
                        if (chatMessage != null) {
                            chattingAdapter.addFileMessage(chatMessage)
                        }
                }
             // 객체를 채팅 어댑터에 추가
            binding.chattingRecyclerView.smoothScrollToPosition(chattingAdapter.itemCount - 1) // 채팅메시지가 표시되는 위치로 스크롤 이동
        }
    }

    private fun parseMessage(topicMessage: StompMessage): ChatMessage {
        // 파싱 로직 구현
        val body = topicMessage.payload // payload를 이용하여 메시지 내용 추출

        val messageJson = JSONObject(body)
        val nicknameJson = JSONObject(body)
        val currentDateTimeJson = JSONObject(body)
        val profileImagePathJson = JSONObject(body)
        val messageTypeJson = JSONObject(body)

        val message = messageJson.getString("message")
        val nickname = nicknameJson.getString("nickname")
        val currentDateTime = currentDateTimeJson.getString("currentDateTime")
        val profileImagePath = profileImagePathJson.getString("profileImagePath")
        val messageType = messageTypeJson.getString("messageType")

        val isMessageType = if (messageType == "FILE") {
            MessageType.FILE
        } else {
            MessageType.CHAT
        }

        Log.e("ChattingFragment parseMessage", "$message, $nickname, $currentDateTime, $profileImagePath, $messageType")

        return ChatMessage(message, "", nickname, currentDateTime, profileImagePath, isMessageType)
    }

    override fun onYesButtonClick(id: Long) {
        val sharedPreferences =
            requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
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
                        Log.d("TokenInterceptor_StudyDeleteFragment", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val chatRoomDeleteService = retrofitBearer.create(ChatRoomDeleteService::class.java)

        chatRoomDeleteService.deleteChatRoom(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.e("ChattingFragment delete chatRoom code", "${response.code()}")
                if (response.isSuccessful) {
                    Toast.makeText(context, "채팅방이 삭제되었습니다", Toast.LENGTH_SHORT).show()

                    val parentFragment = parentFragment
                    if (parentFragment is ChatFragment) {
                        parentFragment.loadChatroomList()
                    }
                }
                requireActivity().supportFragmentManager.popBackStack()

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "채팅방 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }

        })
    }
}