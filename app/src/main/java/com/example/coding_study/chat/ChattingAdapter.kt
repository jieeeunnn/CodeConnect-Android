package com.example.coding_study.chat

import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.common.LoadImageTask
import com.example.coding_study.common.TokenManager
import com.example.coding_study.databinding.ChatMessageBinding
import com.example.coding_study.databinding.ChatMessageReceiveBinding
import com.example.coding_study.databinding.FileMessageBinding
import com.example.coding_study.databinding.FileMessageReceiveBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


data class ChatMessage (
    var message : String,
    var sender: String,
    var nickname: String,
    var currentDateTime: String,
    var profileImagePath: String,
    val messageType: MessageType, // 메시지 유형을 나타내는 타입 필드
)

enum class MessageType {
    CHAT,
    FILE
}

class ChattingAdapter(private val fragment: FragmentActivity, private var chatMessages: MutableList<ChatMessage>,
                      private val tokenManager: TokenManager): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val token = tokenManager.getAccessToken()

    companion object {
        private const val TYPE_MY_MESSAGE = 0
        private const val TYPE_OTHER_MESSAGE = 1
        private const val TYPE_MY_FILE = 2
        private const val TYPE_OTHER_FILE = 3
    }

    override fun getItemViewType(position: Int): Int {
        val message = chatMessages[position]
        return when (message.messageType) {
            MessageType.CHAT -> if (message.sender == "me") TYPE_MY_MESSAGE else TYPE_OTHER_MESSAGE
            MessageType.FILE -> if (message.sender == "me") TYPE_MY_FILE else TYPE_OTHER_FILE
            else -> throw IllegalArgumentException("Invalid message type")
        }
    }


    inner class ChatMessageViewHolder(private val binding: ChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage) {
            binding.chatMessageTextView.text = chatMessage.message
            binding.myMessageNickname.text = chatMessage.nickname
            binding.myMessageCurrentTime.text = chatMessage.currentDateTime

            val imageUrl: String? = "http://52.79.53.62:8080/"+ chatMessage.profileImagePath
            val imageView: ImageView = binding.chatMyImage
            val loadImageTask = LoadImageTask(imageView, token)
            loadImageTask.execute(imageUrl)
        }
    }

    inner class OtherChatMessageViewHolder(private val binding: ChatMessageReceiveBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage) {
            binding.chatReceiveMessage.text = chatMessage.message
            binding.otherMessageNickname.text = chatMessage.nickname
            binding.otherMessageCurrentTime.text = chatMessage.currentDateTime

            val imageUrl: String? = "http://52.79.53.62:8080/"+ "${chatMessage.profileImagePath}"
            val imageView: ImageView = binding.chatOtherImage
            val loadImageTask = LoadImageTask(imageView, token)
            loadImageTask.execute(imageUrl)
        }
    }

    inner class MyFileViewHolder(private val binding: FileMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fileMessage: ChatMessage) {
            binding.fileMessageText.text = fileMessage.message
            binding.fileMessageNickname.text = fileMessage.nickname
            binding.myFileCurrentTimeText.text = fileMessage.currentDateTime

            val imageUrl: String? = "http://152.79.53.62:8080/"+ fileMessage.profileImagePath
            val imageView: ImageView = binding.myFileMessageProfileImage
            val loadImageTask = LoadImageTask(imageView, token)
            loadImageTask.execute(imageUrl)

            val retrofitBearer = Retrofit.Builder()
                .baseUrl("http://52.79.53.62:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                            Log.d("TokenInterceptor_StudyFragment", "Token: $token")
                            chain.proceed(request)
                        }
                        .build()
                )
                .build()

            val sharedPreferencesFilePath = fragment.getSharedPreferences("MyFilePath", Context.MODE_PRIVATE)
            val filePath = sharedPreferencesFilePath?.getString("myFilePath", "") // 저장해둔 파일 경로 가져오기

            val sharedPreferencesFileContent = fragment.getSharedPreferences("MyFileContentType", Context.MODE_PRIVATE)
            val contentType = sharedPreferencesFileContent?.getString("myFileContentType", "") // 저장해둔 파일 타입 가져오기

            val chatFileDownload = retrofitBearer.create(ChatFileDownloadService::class.java)

            binding.myFileDownloadButton.setOnClickListener {
                Log.e("chatting Adapter filePath, fileContentType", "$filePath, $contentType")

                if (filePath != null) {
                    if (contentType != null) {
                        chatFileDownload.fileDownload(filePath = filePath, fileContentType = contentType).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                Log.e("ChattingAdapter file download Button response code", "${response.code()}")
                                Log.e("ChattingAdapter file download Button response body", "${response.body()}")

                                if (response.isSuccessful) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val fileUrl = "http://52.79.53.62:8080/chat/file/download?filePath=$filePath&fileContentType=$contentType"
                                        val savePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/" // 저장할 파일 경로

                                        val connection = URL(fileUrl).openConnection() as HttpURLConnection

                                        val contentDispositionHeader = connection.getHeaderField("Content-Disposition")
                                        val fileName = extractFileNameFromContentDisposition(contentDispositionHeader)

                                        try {
                                            val url = URL(fileUrl)

                                            val connection = url.openConnection() as HttpURLConnection
                                            connection.requestMethod = "GET"

                                            val responseCode = connection.responseCode
                                            if (responseCode == HttpURLConnection.HTTP_OK) {

                                                val inputStream = connection.inputStream
                                                val outputStream =
                                                    FileOutputStream(File(savePath, fileName))

                                                val buffer = ByteArray(4096)
                                                var bytesRead: Int
                                                while (inputStream.read(buffer)
                                                        .also { bytesRead = it } != -1
                                                ) {
                                                    outputStream.write(buffer, 0, bytesRead)
                                                }
                                                outputStream.close()
                                                inputStream.close()
                                                connection.disconnect()

                                                Log.e("file download", "complete")

                                            } else { // 파일 다운로드 실패
                                                Log.e("file download", "fail")
                                            }
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.e("ChattingFragment file upload", "Failed", t)
                            }

                        })
                    }
                }
            }
        }
    }

    inner class OtherFileViewHolder(private val binding: FileMessageReceiveBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fileMessage: ChatMessage) {
            binding.otherFileMessage.text = fileMessage.message
            binding.otherFileNickname.text = fileMessage.nickname
            binding.otherFileCurrentTime.text = fileMessage.currentDateTime

            val imageUrl: String? = "http://52.79.53.62:8080/"+ fileMessage.profileImagePath
            val imageView: ImageView = binding.otherFileProfileImage
            val loadImageTask = LoadImageTask(imageView, token)
            loadImageTask.execute(imageUrl)

            val retrofitBearer = Retrofit.Builder()
                .baseUrl("http://52.79.53.62:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer $token")
                                .build()
                            Log.d("TokenInterceptor_StudyFragment", "Token: $token")
                            chain.proceed(request)
                        }
                        .build()
                )
                .build()

            val sharedPreferencesFilePath = fragment.getSharedPreferences("MyFilePath", Context.MODE_PRIVATE)
            val filePath = sharedPreferencesFilePath?.getString("myFilePath", "") // 저장해둔 파일 경로 가져오기

            val sharedPreferencesFileContent = fragment.getSharedPreferences("MyFileContentType", Context.MODE_PRIVATE)
            val contentType = sharedPreferencesFileContent?.getString("myFileContentType", "") // 저장해둔 파일 타입 가져오기

            val chatFileDownload = retrofitBearer.create(ChatFileDownloadService::class.java)

            binding.otherFileDownloadButton.setOnClickListener {
                Log.e("chatting Adapter filePath, fileContentType", "$filePath, $contentType")
                if (filePath != null) {
                    if (contentType != null) {
                        chatFileDownload.fileDownload(filePath = filePath, fileContentType = contentType).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                Log.e("ChattingAdapter file download Button response code", "${response.code()}")
                                Log.e("ChattingAdapter file download Button response body", "${response.body()}")

                                if (response.isSuccessful) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val fileUrl = "http://52.79.53.62:8080/chat/file/download?filePath=$filePath&fileContentType=$contentType" // 다운받을 파일 url
                                        val savePath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/" // 저장할 파일 경로

                                        val connection = URL(fileUrl).openConnection() as HttpURLConnection

                                        val contentDispositionHeader = connection.getHeaderField("Content-Disposition")
                                        val fileName = extractFileNameFromContentDisposition(contentDispositionHeader)

                                        try {
                                            val url = URL(fileUrl)

                                            val connection = url.openConnection() as HttpURLConnection
                                            connection.requestMethod = "GET"

                                            val responseCode = connection.responseCode
                                            if (responseCode == HttpURLConnection.HTTP_OK) {

                                                val inputStream = connection.inputStream
                                                val outputStream =
                                                    FileOutputStream(File(savePath, fileName))

                                                val buffer = ByteArray(4096)
                                                var bytesRead: Int
                                                while (inputStream.read(buffer)
                                                        .also { bytesRead = it } != -1
                                                ) {
                                                    outputStream.write(buffer, 0, bytesRead)
                                                }
                                                outputStream.close()
                                                inputStream.close()
                                                connection.disconnect()

                                                Log.e("file download", "complete")

                                            } else { // 파일 다운로드 실패
                                                Log.e("file download", "fail")
                                            }
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.e("ChattingFragment file upload", "Failed", t)
                            }

                        })
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MY_MESSAGE -> {
                val binding = ChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ChatMessageViewHolder(binding)
            }
            TYPE_OTHER_MESSAGE -> {
                val binding = ChatMessageReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                OtherChatMessageViewHolder(binding)
            }
            TYPE_MY_FILE -> {
                val binding = FileMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MyFileViewHolder(binding)
            }
            TYPE_OTHER_FILE -> {
                val binding = FileMessageReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                OtherFileViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatMessages[position]
        when (holder) {
            is ChatMessageViewHolder -> {
                val chatMessage = message
                holder.bind(chatMessage)
            }
            is OtherChatMessageViewHolder -> {
                val chatMessage = message
                holder.bind(chatMessage)
            }
            is MyFileViewHolder -> {
                val fileMessage = message
                holder.bind(fileMessage)
            }
            is OtherFileViewHolder -> {
                val fileMessage = message
                holder.bind(fileMessage)
            }

        }
    }

    fun addMessage(message: ChatMessage) {
        chatMessages.add(message)
        notifyItemInserted(chatMessages.size - 1)
    }

    fun addFileMessage(fileMessage: ChatMessage) {
        chatMessages.add(fileMessage)
        notifyItemInserted(chatMessages.size - 1)
    }

    override fun getItemCount(): Int = chatMessages.size


    fun extractFileNameFromContentDisposition(contentDisposition: String?): String { // 서버에서 다운받은 파일 이름 추출
        contentDisposition?.let {
            val fileNameStartIndex = it.indexOf("filename=")
            if (fileNameStartIndex != -1) {
                val fileNameEndIndex = it.indexOf(";", fileNameStartIndex)
                val extractedFileName = if (fileNameEndIndex != -1) {
                    it.substring(fileNameStartIndex + 9, fileNameEndIndex)
                } else {
                    it.substring(fileNameStartIndex + 9)
                }
                return extractedFileName.trim('"')
            }
        }
        return "" // 파일 이름을 추출할 수 없는 경우 빈 문자열("")을 반환
    }

}