package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.StudyHostBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Path

interface StudyDeleteService {
    @DELETE("recruitments/delete/{id}")
    fun deletePost(@Path("id") id: Long): Call<Void>
}


class StudyHostFragment : Fragment(R.layout.study_host) {
    private lateinit var binding: StudyHostBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StudyHostBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("SetTextI18n") // 다국어 지원 기능
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parentFragment = parentFragment
        if (parentFragment is StudyFragment) {
            parentFragment.hideFloatingButton()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()

            val parentFragment = parentFragment
            if (parentFragment is StudyFragment) {
                parentFragment.onResume()
            }
        }

        // 가져온 recruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val gson = Gson()
        val json = arguments?.getString("recruitmentJson")
        val recruitment = gson.fromJson(json, RecruitmentDto::class.java)

        // recruitment 변수에서 게시글 정보를 가져와서 레이아웃에 표시
        binding.hostNicknameText.text = recruitment.nickname
        binding.hostTitleText.text = recruitment.title
        binding.hostContentText.text = recruitment.content
        binding.hostFieldText.text = recruitment.field
        binding.hostCountText.text = "${recruitment.currentCount} / ${recruitment.count}"
        binding.hostCurrentText.text = recruitment.modifiedDateTime ?: recruitment.currentDateTime ?: ""


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
        val studyDeleteService = retrofitBearer.create(StudyDeleteService::class.java)

/*
        class StudyDeleteFragment : DialogFragment() { // 게시글 삭제 여부 다이얼로그
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

                return AlertDialog.Builder(requireContext()).apply {
                    setTitle("게시글 삭제")
                    setMessage("게시글을 삭제 하시겠습니까?")
                    setPositiveButton("예") {dialog, id ->

                        studyDeleteService.deletePost(postId).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.e("StudyHostFragment Delete_response code", "is : ${response.code()}") // 서버 응답 코드 log 출력

                                    Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                                    //글 삭제 후 스터디 게시판으로 돌아감
                                    requireActivity().supportFragmentManager.popBackStack()
                                    dismiss()

                                    /*
                                    val parentFragment = parentFragment
                                    if (parentFragment is StudyFragment) {
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                                     */
                                }
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                // 삭제 요청에 대한 예외 처리
                                Toast.makeText(context, "게시글 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        })
                        println("JoinDialogFragment 확인")}

                    setNegativeButton("아니오") { dialog, id ->
                        println("StudyHostFragment Delete 취소")
                    }
                }.create()
            }
        }

 */

        binding.hostEditButton.setOnClickListener { // 수정 버튼을 눌렀을 때
            val editFragment = StudyEditFragment()
            val bundle = Bundle()
            bundle.putString("recruitmentJson", gson.toJson(recruitment))
            editFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.study_host_fragment, editFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.hostDeleteButton.setOnClickListener { // 삭제 버튼을 눌렀을 때
            val dialogFragment = StudyDeleteFragment()
            dialogFragment.show(childFragmentManager, "deleteDialog")

/*
            studyDeleteService.deletePost(postId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        Log.e("StudyList_response.body", "is : ${response.body()}") // 서버에서 받아온 응답 데이터 log 출력
                        Log.e("response code", "is : ${response.code()}") // 서버 응답 코드 log 출력

                        Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                        //글 삭제 후 스터디 게시판으로 돌아감
                        val parentFragmentManager = requireActivity().supportFragmentManager
                        parentFragmentManager.popBackStackImmediate()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    // 삭제 요청에 대한 예외 처리
                    Toast.makeText(context, "게시글 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })

 */

        }
    }

}
