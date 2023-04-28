package com.example.coding_study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.QnaFragmentBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QnAFragment : Fragment(R.layout.qna_fragment) {
    private lateinit var binding:QnaFragmentBinding
    private lateinit var qnaAdapter: QnaAdapter
    private lateinit var onQnaClickListener: QnaAdapter.OnQnaClickListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.qna_fragment, container, false)
        val binding = QnaFragmentBinding.bind(view)
        val qnaPostRecyclerView = binding.qnaRecyclerView

        // SwipeRefreshLayout 초기화
        binding.qnaSwifeRefreshLayout.setOnRefreshListener {
            loadQnaList()
        }

        var onQnaClickListener: QnaAdapter.OnQnaClickListener = object : QnaAdapter.OnQnaClickListener {
            override fun onQnaClick(position: Int) {
                Log.e("QnaFragment", "onQnaClick!!!")


            }
        }

        qnaAdapter = QnaAdapter(listOf(), onQnaClickListener) // 어댑터 초기화 설정
        qnaPostRecyclerView.adapter = qnaAdapter
        binding.qnaRecyclerView.layoutManager = LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정

        binding.qnaFloatingActionButton.setOnClickListener { // +버튼 (글쓰기 버튼) 눌렀을 때
            val qnaUploadFragment = QnaUpload() // StudyUploadFragment로 변경
            childFragmentManager.beginTransaction()
                .addToBackStack("QNA_FRAGMENT")
                .replace(R.id.qna_fragment_layout, qnaUploadFragment, "QNA_FRAGMENT")
                .commit()
            //binding.floatingActionButton.visibility = View.GONE

        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadQnaList()
    }
    private fun loadQnaList() {
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
                            //.addHeader("Authorization", "Bearer $token")
                            .build()
                        Log.d("TokenInterceptor_StudyFragment", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val qnaService = retrofitBearer.create(QnaGetService::class.java)
        val binding = view?.let { QnaFragmentBinding.bind(it)}

        qnaService.qnagetList().enqueue(object : Callback<QnaListResponse> {
            override fun onResponse(
                call: Call<QnaListResponse>,
                response: Response<QnaListResponse>
            ) {
                if (response.isSuccessful) {
                    val qnaListResponse = response.body()
                    Log.e("QnaList_response.body", "is: $qnaListResponse")
                    Log.e("QnaList_response.code", "is: ${response.code()}")

                    val qnaList = qnaListResponse?.data
                    val qnapostListResponse = qnaList?.map {
                        QnaPost("jieun", it.title, it.content, "2023.04.28")
                        //QnaPost(it.nickname, it.title, it.content, it.currentDateTime)
                    }
                    //qnaList의 형식은 List<QnaUploadDto>이므로 서버에서 받은 게시글을 qnaPostList에 넣어주기 위해 List<qnaPost>로 변환

                    if (qnaListResponse?.result == true) {
                        if (qnapostListResponse != null) {
                            qnaAdapter.qnaPostList = qnapostListResponse
                            qnaAdapter.notifyDataSetChanged()
                        }
                        if (binding != null) {
                            binding.qnaSwifeRefreshLayout.isRefreshing = false // 새로고침 상태를 false로 변경해서 새로고침 완료
                        }

                    }


                }
            }

            override fun onFailure(call: Call<QnaListResponse>, t: Throwable) {
                Log.e("QnaFragment", "Failed to get qna list")
            }

        })
    }
}