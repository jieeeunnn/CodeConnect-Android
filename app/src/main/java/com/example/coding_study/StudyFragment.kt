package com.example.coding_study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.StudyFragmentBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface StudyGetService { // 게시글 조회 인터페이스
    @GET("recruitments/list") // 전체 게시글
    //@GET("main") // 주소, 필드가 같은 게시글
    fun studygetList(
    ): Call<StudyListResponse>
}

data class StudyListResponse ( // 게시글 응답값
    //변수명이 JSON에 있는 키값과 같아야함
    var result: Boolean,
    var message: String,
    var data: List<RecruitmentDto>? // 게시글 데이터를 리스트로 받음
)

interface StudyOnlyService { // 게시글 하나만 조회
    @GET("req/data")
    fun getOnlyPost(
        @Query("recruitmentId") recruitmentId: Long
    ): Call<StudyOnlyResponse>
}

data class StudyOnlyResponse( // 게시글 하나만 조회할 때 응답값 (Map으로 Role 정보 받음)
    var result: Boolean,
    var message: String,
    var data: Map<Role, RecruitmentDto>
)

enum class Role{
    GUEST,
    HOST
}


class StudyFragment : Fragment(R.layout.study_fragment) {
    //private lateinit var viewModel: StudyViewModel
    private lateinit var studyAdapter: StudyAdapter
    private lateinit var onItemClickListener: StudyAdapter.OnItemClickListener


    fun savePostIds(context: Context, postIds: List<Long>) {
        val sharedPreferencesPostId = context.getSharedPreferences("MyPostIds", Context.MODE_PRIVATE)
        val editor = sharedPreferencesPostId.edit()
        val idSet = postIds.toSet()
        idSet.forEachIndexed { index, id ->
            editor.putLong("post_$index", id)
        }
        if (!editor.commit()) {
            Log.e("savePostIds", "Failed to save post IDs")
        }
    }




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
        var onItemClickListener: StudyAdapter.OnItemClickListener = object : StudyAdapter.OnItemClickListener {
            //onItemClickListener = object : StudyAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) { // 게시글 클릭 시
                Log.e("StudyFragment", "onItemClick!!!")

                val sharedPreferencesPostId = requireActivity().getSharedPreferences("MyPostIds", Context.MODE_PRIVATE)
                val postIds = sharedPreferencesPostId.getStringSet("postIds", emptySet())
                // 클릭한 게시물의 아이디를 selectedPostId 변수에 저장
                Log.e("StudyFragment","postIds: $postIds")
                val selectedPostId = postIds?.toList()?.get(position)?.toLong()


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

                val studyOnlyService = retrofitBearer.create(StudyOnlyService::class.java)

                if (selectedPostId != null) {
                    studyOnlyService.getOnlyPost(recruitmentId= selectedPostId).enqueue(object : Callback<StudyOnlyResponse>{
                        override fun onResponse(call: Call<StudyOnlyResponse>, response: Response<StudyOnlyResponse>) {
                            if (response.isSuccessful){
                                val studyOnlyResponse = response.body() // 서버에서 받아온 응답 데이터
                                val code = response.code() // 서버 응답 코드
                                Log.e("StudyOnlyResponse_response.body", "is : ${response.body()}") // 서버에서 받아온 응답 데이터 log 출력
                                Log.e("StudyOnlyResponse_response code", "is : $code") // 서버 응답 코드 log 출력

                                if (studyOnlyResponse?.result == true && studyOnlyResponse.data.containsKey(Role.HOST)){
                                    //호스트 게시글 프래그먼트로 이동 (수정 삭제 버튼이 있는 레이아웃)
                                    /*
                                    val recruitment = studyOnlyResponse.data[Role.HOST] as RecruitmentDto
                                    val bundle = bundleOf("role" to Role.HOST, "recruitment" to recruitment)
                                    val hostFragment = StudyHostFragment()
                                    hostFragment.arguments = bundle

                                     */
                                    val recruitment = studyOnlyResponse.data[Role.HOST] as RecruitmentDto
                                    val gson = Gson()
                                    val json = gson.toJson(recruitment)
                                    val bundle = Bundle()
                                    bundle.putString("recruitmentJson", json)
                                    val hostFragment = StudyHostFragment()
                                    hostFragment.arguments = bundle
                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.study_fragment_layout, hostFragment)
                                        .addToBackStack(null)
                                        .commit()

                                } else if (studyOnlyResponse?.result == true && studyOnlyResponse.data.containsKey(Role.GUEST)){
                                    //게스트 게시글 프래그먼트로 이동 (참여하기 버튼이 있는 레이아웃)
                                    val recruitment = studyOnlyResponse.data[Role.GUEST] as RecruitmentDto
                                    val gson = Gson()
                                    val json = gson.toJson(recruitment)
                                    val bundle = Bundle()
                                    bundle.putString("recruitmentJson", json)
                                    val guestFragment = StudyGuestFragment()
                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.study_fragment_layout, guestFragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                        }

                        override fun onFailure(call: Call<StudyOnlyResponse>, t: Throwable) {
                            Log.e("StudyFragment_StudyOnlyResponse", "Failed to get study list", t)
                            ErrorDialogFragment().show(childFragmentManager, "StudyFragment_Error")                        }
                    })
                }
            }
        }



        //studyAdapter = StudyAdapter(listOf())
        studyAdapter = StudyAdapter(listOf(), onItemClickListener)
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
                    Log.e("StudyList_response.body", "is : ${response.body()}") // 서버에서 받아온 응답 데이터 log 출력
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
                        val recruitmentIds =
                            studyListResponse.data?.map { it.recruitmentId } // 게시물 아이디 리스트 추출
                        if (recruitmentIds != null) {
                            context?.let { savePostIds(it, recruitmentIds) }
                        } // 게시물 아이디 리스트 저장
                        //studyAdapter = StudyAdapter(listOf(), onItemClickListener)
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

    /*
    companion object {
        fun onClick(post: Post) {
            // 게시글 상세화면으로 이동하는 코드 작성
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

            val studyService = retrofitBearer.create(StudyGetService::class.java)
        }
    }

     */

}
