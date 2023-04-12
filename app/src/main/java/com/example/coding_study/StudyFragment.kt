package com.example.coding_study

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.StudyFragmentBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface StudyGetService { // 게시글 조회 인터페이스
    //@GET("recruitments/list") // 전체 게시글
    @GET("main") // 주소, 필드가 같은 게시글
    fun studygetList(
    ): Call<StudyListResponse>
}

data class StudyListResponse ( // 게시글 응답값
    //변수명이 JSON에 있는 키값과 같아야함
    var result: Boolean,
    var message: String,
    var data: List<RecruitmentDto>? // 게시글 데이터를 리스트로 받음
)

class StudyFragment : Fragment(R.layout.study_fragment) {
    //private lateinit var viewModel: StudyViewModel
    private lateinit var studyAdapter: StudyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // 뷰모델 인스턴스 가져오기
        //viewModel = ViewModelProvider(requireActivity()).get(StudyViewModel::class.java)

        // 게시글 목록 뷰 생성
        val view = inflater.inflate(R.layout.study_fragment, container, false)
        val binding = StudyFragmentBinding.bind(view)
        val postRecyclerView = binding.studyRecyclerView

        // SwipeRefreshLayout 초기화
        binding.swipeRefreshLayout.setOnRefreshListener { // 게시판을 swipe해서 새로고침하면 새로 추가된 게시글 업로드
            loadStudyList()
        }

        // 어댑터 설정
        studyAdapter = StudyAdapter(listOf())
        postRecyclerView.adapter = studyAdapter
        binding.studyRecyclerView.layoutManager = LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정


        binding.floatingActionButton.setOnClickListener { // +버튼 (글쓰기 버튼) 눌렀을 때
            val studyuploadFragment = StudyUpload() // StudyUploadFragment로 변경
            childFragmentManager.beginTransaction()
                .add(R.id.study_fragment_layout, studyuploadFragment, "STUDY_FRAGMENT")
                .addToBackStack("STUDY_FRAGMENT")
                .commit()
        }

        return view
    }


    // onResume에서 loadStudyList() 함수 호출
    override fun onResume() {
        super.onResume()
        loadStudyList()
    }


    private fun loadStudyList() { // 서버에서 게시글 전체를 가져와서 로드하는 함수
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

        val studyService = retrofitBearer.create(StudyGetService::class.java)
        val binding = view?.let { StudyFragmentBinding.bind(it) }

        //스터디 게시글 가져오기
        studyService.studygetList().enqueue(object : Callback<StudyListResponse> {
            override fun onResponse(
                call: Call<StudyListResponse>,
                response: Response<StudyListResponse>
            ) {
                if (response.isSuccessful) {
                    val studyListResponse = response.body() // 서버에서 받아온 응답 데이터
                    val code = response.code() // 서버 응답 코드
                    Log.e(
                        "StudyList_response.body",
                        "is : ${response.body()}"
                    ) // 서버에서 받아온 응답 데이터 log 출력
                    Log.e("response code", "is : $code") // 서버 응답 코드 log 출력

                    val studyList = studyListResponse?.data
                    val postListResponse = studyList?.map {
                        Post(
                            it.nickname,
                            it.title,
                            it.content,
                            it.count,
                            it.field,
                            it.currentDateTime.substring(0, 10)
                        )
                    } ?: emptyList()
                    //studyList의 형식은 List<RecruitmentDto>이므로 서버에서 받은 게시글을 postList에 넣어주기 위해 List<Post>로 변환

                    if (studyListResponse?.result == true) {
                        studyAdapter.postList = postListResponse.reversed() // 어댑터의 postList 변수 업데이트 (reversed()를 이용해서 리스트를 역순으로 정렬하여 최신글이 가장 위에 뜨게 됨)
                        studyAdapter.notifyDataSetChanged() // notifyDataSetChanged() 메서드를 호출하여 변경 내용을 화면에 반영

                        if (binding != null) {
                            binding.swipeRefreshLayout.isRefreshing = false // 새로고침 상태를 false로 변경해서 새로고침 완료
                        }
                    }
                }
            }

            override fun onFailure(call: Call<StudyListResponse>, t: Throwable) {
                Log.e("StudyFragment", "Failed to get study list", t)
                ErrorDialogFragment().show(childFragmentManager, "StudyFragment_Error")
            }
        })
    }

}
