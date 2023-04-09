package com.example.coding_study

import android.R
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.coding_study.databinding.WriteStudyBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StudyUpload(val clickedItemPos: Int = -1) : Fragment(),LifecycleOwner { // study 게시판 글쓰기 fragment
    private lateinit var binding: WriteStudyBinding
    private var role: Role? = null

    companion object { // 스피너 목록
        val filters = arrayListOf("안드로이드", "ios", "알고리즘", "데이터베이스", "운영체제", "서버", "웹", "머신러닝", "기타")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WriteStudyBinding.inflate(inflater, container, false)
        //val viewModel = ViewModelProvider(this).get(StudyViewModel::class.java)
        //val viewModel by viewModels<StudyViewModel>()


        //스피너 어댑터 생성
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // 클릭했을 때 아래로 펼쳐지는 레이아웃
        binding.spinner.adapter =
            adapter // spinner와 adapter를 연결하여 spinner는 ArrayAdapter 안에 있는 filters 중에서 선택 가능

        if (clickedItemPos >= 0) { // 생성자 인자로 받은 clickedItemPos가 0보다 크면 해당 데이터를 찾아서 위젯 내용 초기화
            //스피너 초기값 설정
            activity?.let { activity ->
                val viewModel = ViewModelProvider(activity).get(StudyViewModel::class.java)
                // 뷰모델에 액세스할 수 있는 코드
                val f = viewModel.postList.value?.get(clickedItemPos)?.field // 클릭한 아이템의 field값을 가져옴
                val s = filters.indexOf(f) // field 값을 이용하여 filters에서 해당 값의 index를 찾음
                binding.spinner.setSelection(s) // 스피너 선택 값 초기화
            }
        } else {
            binding.spinner.setSelection(0)
        }

        val sharedPreferences =
            requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

        val retrofit = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token.orEmpty())
                            //.addHeader("Authorization", "Bearer $token")
                            .build()
                        Log.d("TokenInterceptor", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val studyService = retrofit.create(StudyService::class.java)

        //업로드 버튼 클릭 시
        activity?.let { activity ->
            val viewModel = ViewModelProvider(activity).get(StudyViewModel::class.java)
            // 뷰모델에 액세스할 수 있는 코드

        binding.buttonUpload.setOnClickListener {
            val sharedPreferences =
                context?.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val nickname = sharedPreferences?.getString("nickname", "") ?: "not found"

            val title = binding.editTitle.text.toString()
            val content = binding.editContent.text.toString()
            val count = binding.editNumber.text.toString().toLong()
            val field = binding.spinner.selectedItem as String // 스피너 선택 값 가져오기
            role = Role.HOST


            val studyRequest = StudyRequest(title, content, count, role!!, field)

            Log.e(
                "studyPost",
                "nickname: $nickname, title: $title, content: $content, count: $count, " +
                        "role: $role, field:$field"
            )
            studyService.requestStudy(studyRequest).enqueue(object : Callback<StudyResponse> {
                override fun onResponse(
                    call: Call<StudyResponse>, response: Response<StudyResponse> // 통신에 성공했을 때
                ) {
                    val code = response.code() // 서버 응답 코드
                    Log.e("response code", "is : $code")

                    if (response.isSuccessful) {
                        val studyResponse = response.body() // 서버에서 받아온 응답 데이터
                        Log.e("StudyPost", "is: ${response.body()}")

                        if (studyResponse?.result == true && studyResponse.data != null) {
                            //val currentDateTime = studyResponse.data!!.currentDateTime // 서버에서 받아온 현재시간
                            fun savePost() {
                                var nickname =
                                    sharedPreferences?.getString("nickname", "") ?: "not found"
                                val title = binding.editTitle.text.toString()
                                val content = binding.editContent.text.toString()
                                val num = binding.editNumber.text.toString().toLong()
                                val field = binding.spinner.selectedItem as String // 스피너 선택 값 가져오기
                                val currentTime =
                                    studyResponse.data!!.currentDateTime // 서버에서 받아온 현재시간
                                val post = Post(nickname, title, content, num, field, currentTime)
                                viewModel.addPost(post)
                                Log.e("StudyUploadFragment_viewModel.addPost", "$post added to ViewModel")
                            }
                            savePost()
                            //val newPost = Post(nickname, title, content, count, field, currentDateTime) //새로운 포스트 객체 생성
                            //viewModel.addPost(newPost)
                        }
                    }
                }

                override fun onFailure(call: Call<StudyResponse>, t: Throwable) { // 통신에 실패했을 때
                    Toast.makeText(context, "통신에 실패했습니다", Toast.LENGTH_LONG).show()
                }
            })

            //dismiss() // 다이얼로그 종료

            //업로드 후 리스트로 돌아감
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStackImmediate()

        }
    }

        return binding.root
    }


}
    /*

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // 클릭했을 때 아래로 펼쳐지는 레이아웃
        binding.spinner.adapter = adapter // spinner와 adapter를 연결하여 spinner는 ArrayAdapter 안에 있는 filters 중에서 선택 가능

        if (clickedItemPos >= 0) { // 생성자 인자로 받은 clickedItemPos가 0보다 크면 해당 데이터를 찾아서 위젯 내용 초기화
            val f = viewModel.items[clickedItemPos].field
            val s = filters.indexOf(f)
            binding.spinner.setSelection(s) // 스피너 선택 값 초기화

            binding.editTitle.setText(viewModel.items[clickedItemPos].title)
            binding.editNumber.setText(viewModel.items[clickedItemPos].num)
            binding.editContent.setText(viewModel.items[clickedItemPos].content)

        } else {
            binding.spinner.setSelection(0)
        }

        binding.spinner.setSelection(0)


        binding.buttonUpload.setOnClickListener{



            val field = binding.spinner.selectedItem as String // 스피너 선택 값 가져오기
            val nickname = "hansung"
            val title = binding.editTitle.text.toString()
            val content = binding.editContent.text.toString()
            val num = binding.editNumber.text.toString()

            val item = Item(nickname, title, content, field, num)

            if (clickedItemPos < 0) {
                viewModel.addItem(item)
                Log.e("tag", "uploadButton_addItem")
            }
            else
                viewModel.updateItem(item, clickedItemPos)



            dismiss()

            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStackImmediate()
        }
    }
    companion object { // 스피너 목록
        val filters = arrayListOf("안드로이드", "ios", "알고리즘", "데이터베이스", "운영체제", "서버", "웹", "머신러닝", "기타")
    }
}

     */