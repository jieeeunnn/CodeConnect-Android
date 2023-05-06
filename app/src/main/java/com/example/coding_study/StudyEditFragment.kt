package com.example.coding_study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.WriteStudyBinding
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


interface StudyEditService{
    @PUT("recruitments/update/{id}")
    fun editPost(@Path("id") id: Long, @Body studyEdit: StudyRequest): Call<StudyResponse>
}


class StudyEditFragment : Fragment(R.layout.write_study) {
    private lateinit var binding: WriteStudyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 이전 프래그먼트에서 전달받은 글 정보
        binding = WriteStudyBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()
        }

        val gson = Gson()
        val json = arguments?.getString("recruitmentJson")
        val recruitment = gson.fromJson(json, RecruitmentDto::class.java)

        binding.editTitle.setText(recruitment.title)
        binding.editContent.setText(recruitment.content)
        binding.editNumber.setText(recruitment.count.toString())

        val sharedPreferencesFields = requireActivity().getSharedPreferences("MyFields", Context.MODE_PRIVATE)
        val fieldsString = sharedPreferencesFields?.getString("fields", "")
        val fields = fieldsString?.split(",") ?: emptyList()

        //스피너 어댑터 생성
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fields)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // 클릭했을 때 아래로 펼쳐지는 레이아웃
        binding.spinner.adapter = adapter // spinner와 adapter를 연결하여 spinner는 ArrayAdapter 안에 있는 fields 중에서 선택 가능

        // recruitment 객체에서 필드 값 가져와서 스피너 초기값 설정
        val field = recruitment.field
        val filterIndex = fields.indexOf(field)
        binding.spinner.setSelection(filterIndex)


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

        val postId = recruitment.recruitmentId
        val studyEditService = retrofitBearer.create(StudyEditService::class.java)

        binding.buttonUpload.setOnClickListener {
            val title = binding.editTitle.text.toString()
            val content = binding.editContent.text.toString()
            val field = binding.spinner.selectedItem as String // 스피너 선택 값 가져오기
            val count = binding.editNumber.text.toString().toLong()

            val studyEdit = StudyRequest(title, content, count, field) // 서버에 보낼 요청값

            studyEditService.editPost(postId, studyEdit).enqueue(object : Callback<StudyResponse>{
                override fun onResponse(call: Call<StudyResponse>, response: Response<StudyResponse>) {
                    if (response.isSuccessful){
                        Log.e("response code", "is : ${response.code()}")
                        Log.e("StudyEditFragment_response.body", "is : ${response.body()}") // 서버에서 받아온 응답 데이터 log 출력


                        // 수정된 글을 서버에서 받아와서 StudyHostFragment로 다시 전달
                        val bundle = Bundle()
                        bundle.putString("recruitmentJson", gson.toJson(response.body()))
                        val studHostFragment = StudyHostFragment()
                        studHostFragment.arguments = bundle

                        val parentFragmentManager = requireActivity().supportFragmentManager
                        parentFragmentManager.popBackStack()
                        parentFragmentManager.popBackStack() // popBackStack()을 두번 호출해서 StudyFragment로 이동

                        val parentFragment = parentFragment
                        if (parentFragment is StudyFragment) {
                            parentFragment.onResume()
                        }

                    }else{
                        Log.e("StudyEditFragment_onResponse", "But not success")
                    }
                }

                override fun onFailure(call: Call<StudyResponse>, t: Throwable) {
                    Toast.makeText(context, "통신에 실패했습니다", Toast.LENGTH_LONG).show()
                }
            })

        }

        return binding.root
    }

}