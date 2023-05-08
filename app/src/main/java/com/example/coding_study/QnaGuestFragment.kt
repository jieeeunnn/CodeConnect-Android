package com.example.coding_study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.QnaGuestBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QnaGuestFragment : Fragment(R.layout.qna_guest) {
    private lateinit var binding: QnaGuestBinding
    private lateinit var qnaCommentAdapter: QnaCommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QnaGuestBinding.inflate(inflater, container, false)

        // 가져온 qnaRecruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaGuestRecruitmentJson")
        val commentHostJson = arguments?.getString("qnaCommentHostJson")
        val commentGuestJson = arguments?.getString("qnaCommentGuestJson")

        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)
        val commentHost = qnaGson.fromJson(commentHostJson, QnaCommentListResponse::class.java)
        val commentGuest = qnaGson.fromJson(commentGuestJson, QnaCommentListResponse::class.java)

        if (commentHost != null && commentGuest != null) {
            val commentHostList = commentHost.comments.map { QnaComment(it.nickname, it.comment, it.currentDateTime, it.commentId) }
            val commentGuestList = commentGuest.comments.map { QnaComment(it.nickname, it.comment, it.currentDateTime, it.commentId) }

            qnaCommentAdapter = QnaCommentAdapter(commentHostList, commentGuestList)
            val qnaHostRecyclerView = binding.qnaGuestRecyclerView
            qnaHostRecyclerView.adapter = qnaCommentAdapter
            binding.qnaGuestRecyclerView.layoutManager = LinearLayoutManager(context)
        }

        binding.qnaGuestNickname.text = qnaRecruitment.nickname
        binding.qnaGuestTitle.text = qnaRecruitment.title
        binding.qnaGuestContent.text = qnaRecruitment.content
        binding.qnaGuestCurrentTime.text = qnaRecruitment.currentDateTime

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
                parentFragment.onResume()
            }
        }
    }
}