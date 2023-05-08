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
import com.example.coding_study.databinding.QnaHostBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QnaHostFragment : Fragment(R.layout.qna_host), DeleteDialogInterface{
    private lateinit var binding: QnaHostBinding
    private lateinit var qnaCommentAdapter: QnaCommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QnaHostBinding.inflate(inflater, container, false)

        // 가져온 qnaRecruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaHostRecruitmentJson")
        val commentHostJson = arguments?.getString("commentHostJson")
        val commentGuestJson = arguments?.getString("commentGuestJson")

        val qnaRecruitment = qnaGson.fromJson(qnaJson,QnaUploadDto::class.java)
        val commentHost = qnaGson.fromJson(commentHostJson, QnaCommentListResponse::class.java)
        val commentGuest = qnaGson.fromJson(commentGuestJson, QnaCommentListResponse::class.java)

        if (commentHost != null && commentGuest != null) {
            val commentHostList = commentHost.comments.map { QnaComment(it.nickname, it.comment, it.currentDateTime, it.commentId) }
            val commentGuestList = commentGuest.comments.map { QnaComment(it.nickname, it.comment, it.currentDateTime, it.commentId) }

            qnaCommentAdapter = QnaCommentAdapter(commentHostList, commentGuestList)
            val qnaHostRecyclerView = binding.qnaHostRecyclerView
            qnaHostRecyclerView.adapter = qnaCommentAdapter
            binding.qnaHostRecyclerView.layoutManager = LinearLayoutManager(context)
        }


        binding.qnaHostNickname.text = qnaRecruitment.nickname
        binding.qnaHostTItle.text = qnaRecruitment.title
        binding.qnaHostContent.text = qnaRecruitment.content
        binding.qnaHostCurrentTime.text = qnaRecruitment.currentDateTime


        //저장된 토큰값 가져오기
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
                        Log.d("TokenInterceptor_StudyFragment", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        //qna 삭제 버튼
        binding.qnaHostDeleteButton.setOnClickListener {
            val deleteDialog = DeleteDialog(this, qnaRecruitment.qnaId)
            deleteDialog.isCancelable = false
            deleteDialog.show(this.childFragmentManager, "deleteDialog")
        }

        //qna 수정 버튼
        binding.qnaHostEditButton.setOnClickListener {
            val qnaEditFragment = QnaEditFragment()
            val qnaBundle = Bundle()
            qnaBundle.putString("qnaRecruitmentJson", qnaGson.toJson(qnaRecruitment))
            qnaEditFragment.arguments = qnaBundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.qna_host_fragment, qnaEditFragment)
                .addToBackStack(null)
                .commit()
        }

        val qnaCommentCreateService = retrofitBearer.create(QnaCommentCreateService::class.java)

        // 댓글 버튼
        binding.hostCommentButton.setOnClickListener {
            val comment = binding.hostComment.text.toString()
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
        super.onViewCreated(view, savedInstanceState)

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

    override fun onYesButtonClick(id: Long) { // 삭제 다이얼로그 확인 버튼 클릭시 게시글 삭제
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

        val qnaDeleteService = retrofitBearer.create(QnaDeleteService::class.java)

        qnaDeleteService.qnaDeletePost(id).enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.e("QnaHostFragment Delete Id----------------------------", "$id")
                if (response.isSuccessful) {
                    Log.e("QnaHostFragment Delete_response code", "${response.code()}")

                    val parentFragment = parentFragment
                    if (parentFragment is StudyFragment) {
                        parentFragment.onResume()
                    }

                    //글 삭제 후 스터디 게시판으로 돌아감
                    requireActivity().supportFragmentManager.popBackStack()

                    Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "게시글 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }

        })

    }
}