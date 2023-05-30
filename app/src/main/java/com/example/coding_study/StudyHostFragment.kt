package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.StudyHostBinding
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class StudyHostFragment : Fragment(R.layout.study_host), DeleteDialogInterface {
    private lateinit var binding: StudyHostBinding

    fun onBackPressed() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        }
    }

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

            val parentFragment = parentFragment
            if (parentFragment is StudyFragment) {
                val parentFragmentManager = requireActivity().supportFragmentManager
                parentFragmentManager.popBackStack()

                parentFragment.showFloatingButton()
            }
            else if (parentFragment is MyPageMyStudy) {
                onBackPressed()
            }
            else if (parentFragment is MyPageParticipateStudy) {
                onBackPressed()
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
        binding.hostAddress2.text = recruitment.address

        val imageUrl: String? = "http://112.154.249.74:8080/"+ "${recruitment.profileImagePath}"
        val imageView: ImageView = binding.studyHostImageView
        val loadImageTask = LoadImageTask(imageView)
        loadImageTask.execute(imageUrl)


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
            val deleteDialog = DeleteDialog(this, recruitment.recruitmentId, "게시글을 삭제하시겠습니까?")
            deleteDialog.isCancelable = false
            deleteDialog.show(this.childFragmentManager, "deleteDialog")

        }
    }

    override fun onYesButtonClick(id: Long) {
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

        val studyDeleteService = retrofitBearer.create(StudyDeleteService::class.java)

        studyDeleteService.deletePost(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Log.e("StudyHostFragment Delete_response code", "is : ${response.code()}") // 서버 응답 코드 log 출력

                    val parentFragment = parentFragment
                    if (parentFragment is StudyFragment) {
                        parentFragment.showFloatingButton()
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
