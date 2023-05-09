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
import com.example.coding_study.databinding.WriteQnaBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

class QnaEditFragment : Fragment(R.layout.write_qna) {
    private lateinit var binding: WriteQnaBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WriteQnaBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()
        }

        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaRecruitmentJson")
        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)

        binding.qnaEditTitle.setText(qnaRecruitment.title)
        binding.qnaEditContent.setText(qnaRecruitment.content)

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
        val qnaEditService = retrofitBearer.create(QnaEditService::class.java)

        binding.qnaButtonUpload.setOnClickListener {
            val title = binding.qnaEditTitle.text.toString()
            val content = binding.qnaEditContent.text.toString()

            val qnaEdit = QnaRequest(title, content) // 서버에 보낼 요청값

            qnaEditService.qnaEditPost(qnaPostId, qnaEdit).enqueue(object : Callback<QnaResponse>{
                override fun onResponse(call: Call<QnaResponse>, response: Response<QnaResponse>) {
                    if (response.isSuccessful) {
                        Log.e("qnaEditPost response code is", "${response.code()}")
                        Log.e("qnaEditPost response body is", "${response.body()}")

                        // 수정된 글을 서버에서 받아와서 QnaHostFragment로 다시 전달
                        val qnaBundle = Bundle()
                        qnaBundle.putString("qnaRecruitmentJson", qnaGson.toJson(response.body()))
                        val qnaHostFragment = QnaHostFragment()
                        qnaHostFragment.arguments = qnaBundle

                        val parentFragment = parentFragment
                        if (parentFragment is QnAFragment) {
                            parentFragment.showFloatingButton()
                        }

                        val parentFragmentManager = requireActivity().supportFragmentManager
                        parentFragmentManager.popBackStack()
                        parentFragmentManager.popBackStack() // popBackStack()을 두번 호출해서 StudyFragment로 이동

                    }else {
                        Log.e("QnaEditFragment_onResponse","But not success")
                    }
                }

                override fun onFailure(call: Call<QnaResponse>, t: Throwable) {
                    Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                }
            })
        }

        return binding.root
    }
}