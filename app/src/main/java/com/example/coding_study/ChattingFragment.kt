package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.coding_study.databinding.ChatMemberListBinding
import com.example.coding_study.databinding.ChattingFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
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


        chattingAdapter = ChattingAdapter(mutableListOf())
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
                                    profileImagePath = chat["profileImagePath"] as String
                                )
                            }
                        }
                    }

                    // 채팅 내용 어댑터에 띄우기
                    convertedChatList?.forEach { chat ->
                        val sender = if (chat.nickname == myNickname) "me" else ""
                        val chatMessage = ChatMessage(
                            chat.message,
                            sender,
                            chat.nickname,
                            chat.currentDateTime,
                            chat.profileImagePath
                        )
                        chattingAdapter.addMessage(chatMessage)
                    }
                    Log.e("ChattingFrgment chatList", convertedChatList.toString())

                    val nicknameImageMap = chatMap?.get("NICKNAME_IMAGE") as? Map<String, String>
                    val memberInfoMap = HashMap<String, String>()
                    Log.e("chattingFragment memberInfoMap", "$memberInfoMap")

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
                    sendMessage(message, roomId, myNickname)
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

        val sharedPreferences = requireActivity().getSharedPreferences("MyImagePath", Context.MODE_PRIVATE)
        val myImagePath = sharedPreferences?.getString("imagePath", "") // 저장해둔 토큰값 가져오기

        data.put("profileImagePath", myImagePath)

        val chatMessage = myImagePath?.let { ChatMessage(message, "me", nickname, currentDateTime, myImagePath) }

        stompClient?.send("/pub/chat/message", data.toString())?.subscribe()

        Log.e("ChattingFragment sendMessage", "$message, $roomId, $nickname, $myImagePath")

        coroutineScope.launch {
            if (chatMessage != null) {
                    chattingAdapter.addMessage(chatMessage)
            } // 객체를 채팅 어댑터에 추가
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

        val message = messageJson.getString("message")
        val nickname = nicknameJson.getString("nickname")
        val currentDateTime = currentDateTimeJson.getString("currentDateTime")
        val profileImagePath = profileImagePathJson.getString("profileImagePath")

        Log.e("ChattingFragment parseMessage", "$message, $nickname, $currentDateTime, $profileImagePath")

        return ChatMessage(message, "", nickname, currentDateTime, profileImagePath)
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