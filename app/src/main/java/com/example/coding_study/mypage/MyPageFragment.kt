package com.example.coding_study.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.dialog.DeleteMemberDialog
import com.example.coding_study.common.LoadImageTask
import com.example.coding_study.R
import com.example.coding_study.databinding.MypageFragmentBinding
import com.example.coding_study.dialog.LogoutDialog
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

class MyPageFragment: Fragment(R.layout.mypage_fragment) {
    private lateinit var binding: MypageFragmentBinding
    private lateinit var myPageProfileView: View
    private lateinit var myPageAdapter: MyPageAdapter
    private val myPageViewModel: MyPageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MypageFragmentBinding.inflate(inflater, container, false)

        myPageProfileView = binding.myPageProfileView

        val myPageRecyclerView = binding.myPageRecyclerView

        var onItemClickListener: MyPageAdapter.OnItemClickListener = object :
            MyPageAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.e("MypageFragment", "onclick")

                if (position == 0) { // 내 프로필 수정
                    childFragmentManager.beginTransaction()
                        .replace(R.id.myPageProfileView, MyPageEditFragment())
                        .addToBackStack(null)
                        .commit()
                }
                if (position == 1) { // 신청한 스터디
                    childFragmentManager.beginTransaction()
                        .replace(R.id.myPageProfileView, MyPageParticipateStudy())
                        .addToBackStack(null)
                        .commit()
                }
                if (position == 2) { // 내가 작성한 스터디
                    childFragmentManager.beginTransaction()
                        .replace(R.id.myPageProfileView, MyPageMyStudy())
                        .addToBackStack(null)
                        .commit()
                }
                if (position == 3) { // 내가 작성한 Q&A
                    childFragmentManager.beginTransaction()
                        .replace(R.id.myPageProfileView, MyPageMyQna())
                        .addToBackStack(null)
                        .commit()
                }
                if (position == 4 ) { // 로그아웃
                    val logoutDialog = LogoutDialog()
                    logoutDialog.isCancelable = false
                    logoutDialog.show(parentFragmentManager, "logoutDialog")
                }
                if (position == 5) { // 회원 탈퇴
                    val deleteDialog = DeleteMemberDialog()
                    deleteDialog.isCancelable = false
                    parentFragmentManager.let { deleteDialog.show(it, "deleteMemberDialog") }
                }
            }
        }

        val textList = listOf("내 프로필 수정","신청한 스터디", "내가 작성한 스터디", "내가 작성한 Q&A", "로그아웃", "회원 탈퇴")
        myPageAdapter = MyPageAdapter(textList, onItemClickListener)
        myPageRecyclerView.adapter = myPageAdapter
        binding.myPageRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadMyPage()
    }

    private fun loadMyPage() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://52.79.53.62:8080/")
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

        val sharedPreferences2 = requireActivity().getSharedPreferences("MyNickname", Context.MODE_PRIVATE)
        val nickname = sharedPreferences2?.getString("nickname", "")

        val myPageService = retrofitBearer.create(MyPageGetService::class.java)
        if (nickname != null) {
            myPageService.myPageGetProfile(nickname).enqueue(object : Callback<MyPageProfileResponse>{
                override fun onResponse(call: Call<MyPageProfileResponse>, response: Response<MyPageProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        val myPageResponse = response.body()
                        Log.e("MyPageFragment response body", "$myPageResponse")
                        Log.e("MyPageFragment response code", "${response.code()}")

                        val gson = Gson()
                        val myPageData = myPageResponse?.data
                        val myPageDto = gson.fromJson(gson.toJson(myPageData), MyProfile::class.java)
                        val myProfile: MyProfile = myPageDto

                        myPageViewModel.setMyProfile(myProfile)

                        if (myPageResponse != null) {
                            binding.myPageNickname.text = myProfile.nickname
                            binding.myPageAddress.text = myProfile.address
                            binding.myPageField1.text = myProfile.fieldList[0]
                            binding.myPageField2.text = myProfile.fieldList[1]

                            val imageUrl: String? = "http://52.79.53.62:8080/"+ "${myProfile.profileImagePath}"
                            val imageView: ImageView = binding.myPageImage
                            val loadImageTask = LoadImageTask(imageView)
                            loadImageTask.execute(imageUrl)
                        }
                    }
                }

                override fun onFailure(call: Call<MyPageProfileResponse>, t: Throwable) {
                    Log.e("MyPageFragment", "Failed to get profile", t)
                    Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}