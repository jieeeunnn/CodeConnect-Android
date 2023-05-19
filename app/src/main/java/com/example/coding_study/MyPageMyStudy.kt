package com.example.coding_study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.MypageMyStudyBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyPageMyStudy: Fragment(R.layout.mypage_my_study) { // 내가 작성한 스터디 게시글 프래그먼트
    private lateinit var studyAdapter: StudyAdapter
    private lateinit var binding: MypageMyStudyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MypageMyStudyBinding.inflate(inflater, container, false)
        val myPageRecyclerView = binding.myPageMyStudyRecyclerView

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

        var onItemClickListener: StudyAdapter.OnItemClickListener = object : StudyAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
// 저장된 게시글 id 가져오기
                val sharedPreferencesPostId = requireActivity().getSharedPreferences("MyPostIds", Context.MODE_PRIVATE) // "MyPostIds" 라는 이름으로 SharedPreferences 객체를 생성
                val size = sharedPreferencesPostId.all.size // SharedPreferences 객체에 저장된 모든 키-값 쌍의 개수를 구함
                val postIds = (0 until size).mapNotNull { // 0부터 size-1까지의 정수를 순회하면서, 해당하는 키("post_0", "post_1", ...)에 대한 값을 리스트에 추가, 함수를 적용한 결과 중 null이 아닌 값들로만 리스트를 만듬
                    val postId = sharedPreferencesPostId.getLong("post_$it", -1) // "post_$it"라는 이름으로 저장된 Long 타입의 값 가져오기, 해당 키가 존재하지 않으면 -1 반환
                    if (postId != -1L) postId else null// postId가 -1L 이 아닐 경우 해당 값을 유지하고, -1L일 경우 null 반환
                }
                Log.e("StudyFragment","postIds: $postIds")

                val selectedPostId = postIds.getOrNull(position)// postIds 리스트에서 position에 해당하는 인덱스의 값을 가져옴
                Log.e("StudyFragment","selectedPostId: $selectedPostId")

                val studyOnlyService = retrofitBearer.create(StudyOnlyService::class.java)

                if (selectedPostId != null) {
                    studyOnlyService.getOnlyPost(selectedPostId).enqueue(object :
                        Callback<StudyOnlyResponse> {
                        override fun onResponse(call: Call<StudyOnlyResponse>, response: Response<StudyOnlyResponse>) {

                            if (response.isSuccessful){
                                val studyOnlyResponse = response.body() // 서버에서 받아온 응답 데이터
                                val code = response.code() // 서버 응답 코드
                                Log.e("StudyOnlyResponse_response.body", "is : ${response.body()}") // 서버에서 받아온 응답 데이터 log 출력
                                Log.e("StudyOnlyResponse_response.code", "is : $code") // 서버 응답 코드 log 출력

                                if (studyOnlyResponse?.result == true && studyOnlyResponse.data.containsKey(Role.HOST)){ // Role이 호스트인 경우
                                    // StudyHostFragment로 게시글 정보를 넘겨주기 위해 받은 데이터 저장
                                    val recruitment = studyOnlyResponse.data[Role.HOST] as Any
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
                                }
                            }
                            else{
                                Log.e("studyOnlyResponse onResponse","But not success")
                            }
                        }
                        override fun onFailure(call: Call<StudyOnlyResponse>, t: Throwable) {
                            Log.e("StudyFragment_StudyOnlyResponse", "Failed to get study list", t)
                            Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }

        studyAdapter = StudyAdapter(listOf(), onItemClickListener)
        myPageRecyclerView.adapter = studyAdapter
        binding.myPageMyStudyRecyclerView.layoutManager = LinearLayoutManager(context)







        return binding.root
    }
}