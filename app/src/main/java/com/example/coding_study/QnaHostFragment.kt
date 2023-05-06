package com.example.coding_study

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.QnaHostBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Path


interface QnaDeleteService {
    @DELETE("qna/delete/{qnaId}")
    fun qnaDeletePost(@Path("qnaId") id: Long): Call<Void>
}


class QnaHostFragment : Fragment(R.layout.qna_host){
    private lateinit var binding: QnaHostBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QnaHostBinding.inflate(inflater, container, false)

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

        // 가져온 qnaRecruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaRecruitmentJson")
        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)

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
                            //.addHeader("Authorization", "Bearer $token")
                            .build()
                        Log.d("TokenInterceptor_StudyFragment", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val qnaPostId = qnaRecruitment.qnaId
        val qnaDeleteService = retrofitBearer.create(QnaDeleteService::class.java)

        class QnaDeleteFragment : DialogFragment() { // 게시글 삭제 여부 다이얼로그
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

                return AlertDialog.Builder(requireContext()).apply {
                    setTitle("게시글 삭제")
                    setMessage("게시글을 삭제 하시겠습니까?")
                    setPositiveButton("예") {dialog, id ->

                        qnaDeleteService.qnaDeletePost(qnaPostId).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.e("QnaList_response.body", "is : ${response.body()}") // 서버에서 받아온 응답 데이터 log 출력
                                    Log.e("response code", "is : ${response.code()}") // 서버 응답 코드 log 출력

                                    Toast.makeText(context, "QnA 게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                                    //글 삭제 후 스터디 게시판으로 돌아감
                                    //requireActivity().supportFragmentManager.popBackStack()

                                    /*
                                    val parentFragment = parentFragment
                                    if (parentFragment is StudyHostFragment) {
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                                     */
                                    dismiss()

                                }
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                // 삭제 요청에 대한 예외 처리
                                Toast.makeText(context, "게시글 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        })
                        println("QnaHostFragment 확인")}

                    setNegativeButton("아니오") { dialog, id ->
                        println("QnaHostFragment Delete 취소")
                    }
                }.create()
            }
        }

        //qna 삭제 버튼
        binding.qnaHostDeleteButton.setOnClickListener {
            val qnaDeleteDialog= QnaDeleteFragment()
            qnaDeleteDialog.show(childFragmentManager, "QnaDeleteDialog")
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


    }
}