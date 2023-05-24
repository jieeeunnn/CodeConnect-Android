package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.QnaGuestBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QnaGuestFragment : Fragment(R.layout.qna_guest) {
    private lateinit var binding: QnaGuestBinding
    private lateinit var qnaCommentAdapter: QnaCommentAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QnaGuestBinding.inflate(inflater, container, false)

        // 가져온 qnaRecruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaGuestRecruitmentJson")
        val commentHostJson = arguments?.getString("commentHostJson")
        val commentGuestJson = arguments?.getString("commentGuestJson")

        Log.e("QnaGuestFragment adapter ", "$qnaJson, $commentHostJson")

        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)
        val commentHost = qnaGson.fromJson<List<Comment>>(commentHostJson, object : TypeToken<List<Comment>>() {}.type)
        val commentGuest = qnaGson.fromJson<List<Comment>>(commentGuestJson, object : TypeToken<List<Comment>>() {}.type)

        var commentList: List<Comment> = ((commentHost ?: emptyList()) + (commentGuest ?: emptyList())).sortedBy { it.currentDateTime }

        if (commentHost != null || commentGuest != null) {
            val qnaGuestRecyclerView = binding.qnaGuestRecyclerView
            qnaCommentAdapter = QnaCommentAdapter(fragmentManager = childFragmentManager, commentList)
            qnaGuestRecyclerView.adapter = qnaCommentAdapter
            binding.qnaGuestRecyclerView.layoutManager = LinearLayoutManager(context)

            qnaCommentAdapter.notifyDataSetChanged()
        }else{
            commentList = emptyList()
            qnaCommentAdapter = QnaCommentAdapter(childFragmentManager, commentList)
            binding.qnaGuestRecyclerView.adapter = qnaCommentAdapter
            binding.qnaGuestRecyclerView.layoutManager = LinearLayoutManager(context)

            qnaCommentAdapter.notifyDataSetChanged()
        }

        binding.qnaGuestNickname.text = qnaRecruitment.nickname
        binding.qnaGuestTitle.text = qnaRecruitment.title
        binding.qnaGuestContent.text = qnaRecruitment.content
        binding.qnaGuestCurrentTime.text = qnaRecruitment.currentDateTime

        val imageUrl: String? = "http://112.154.249.74:8080/"+ "${qnaRecruitment.imagePath}"
        val imageView: ImageView = binding.qnaGuestImageView
        val loadImageTask = LoadImageTask(imageView)
        loadImageTask.execute(imageUrl)

        binding.qnaGuestSwifeRefreshLayout.setOnRefreshListener {
            loadQnaGuest()
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
                        Log.d("TokenInterceptor", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val qnaCommentCreateService = retrofitBearer.create(QnaCommentCreateService::class.java)

        binding.guestCommentButton.setOnClickListener { // 댓글 작성 버튼
            val comment = binding.guestCommentEdit.text.toString()
            val qnaId = qnaRecruitment.qnaId
            val qnaCommentRequest = QnaCommentRequest(comment)

            qnaCommentCreateService.qnaCommentCreate(qnaId, qnaCommentRequest).enqueue(object : Callback<QnaCommentResponse>{
                override fun onResponse(call: Call<QnaCommentResponse>, response: Response<QnaCommentResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.e("Qna post comment response code", "${response.code()}")
                        Log.e("Qna post comment response body", "${response.body()}")
                        binding.guestCommentEdit.text = null

                        loadQnaGuest()
                    }
                }

                override fun onFailure(call: Call<QnaCommentResponse>, t: Throwable) {
                    Toast.makeText(context, "qna comment 서버 연결 실패", Toast.LENGTH_LONG).show()
                }
            })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val parentFragment = parentFragment
        if (parentFragment is QnAFragment) {
            parentFragment.hideFloatingButton()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()

            val parentFragment = parentFragment
            if (parentFragment is QnAFragment) {
                parentFragment.showFloatingButton()
            }
        }
    }

    fun loadQnaGuest() {
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

        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaGuestRecruitmentJson")
        val qnaRecruitment = qnaGson.fromJson(qnaJson,QnaUploadDto::class.java)

        val qnaId = qnaRecruitment.qnaId
        val qnaOnlyService = retrofitBearer.create(QnaOnlyService::class.java)

        qnaOnlyService.qnaGetOnlyPost(qnaId).enqueue(object : Callback<QnaOnlyResponse>{
            override fun onResponse(
                call: Call<QnaOnlyResponse>,
                response: Response<QnaOnlyResponse>
            ) {
                val qnaOnlyResponse = response.body()
                Log.e("QnaOnlyResponse_reponse.body", "is : $qnaOnlyResponse")
                Log.e("QnaOnlyResponse_response.code", "is : ${response.code()}")

                if (qnaOnlyResponse?.result == true && qnaOnlyResponse.data.containsKey(QnaRole.GUEST)) {
                    val qnaGuest = qnaOnlyResponse.data[QnaRole.GUEST] as Any
                    val qnaUploadDto = qnaGson.fromJson(qnaGson.toJson(qnaGuest), QnaUploadDto::class.java)

                    binding.qnaGuestNickname.text = qnaUploadDto.nickname
                    binding.qnaGuestTitle.text = qnaUploadDto.title
                    binding.qnaGuestContent.text = qnaUploadDto.content
                    binding.qnaGuestCurrentTime.text = qnaUploadDto.currentDateTime

                    val commentHost = qnaOnlyResponse.data[QnaRole.COMMENT_HOST] as? List<Comment>
                    val commentGuest = qnaOnlyResponse.data[QnaRole.COMMENT_GUEST] as? List<Comment>
                    Log.e("QnaHostFragment LoadQnaHost commentHost", "$commentHost")
                    Log.e("QnaHostFragment LoadQnaHost commentGuest", "$commentGuest")

                    val commentHostJson = qnaGson.toJson(commentHost)
                    val commentGuestJson = qnaGson.toJson(commentGuest)

                    val commentHostList = qnaGson.fromJson<List<Comment>>(commentHostJson, object : TypeToken<List<Comment>>() {}.type)
                    val commentGuestList = qnaGson.fromJson<List<Comment>>(commentGuestJson, object : TypeToken<List<Comment>>() {}.type)

                    var commentList: List<Comment> = ((commentHostList ?: emptyList()) + (commentGuestList ?: emptyList())).sortedBy { it.currentDateTime }

                    qnaCommentAdapter.commentList = commentList
                    qnaCommentAdapter.notifyDataSetChanged()

                    binding.qnaGuestSwifeRefreshLayout.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<QnaOnlyResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

    }
}