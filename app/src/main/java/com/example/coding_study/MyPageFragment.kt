package com.example.coding_study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.MypageFragmentBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyPageFragment: Fragment(R.layout.mypage_fragment) {
    private lateinit var binding: MypageFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MypageFragmentBinding.inflate(inflater, container, false)

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

        val myPageService = retrofitBearer.create(MyPageGetService::class.java)
        myPageService.myPageGetProfile().enqueue(object : Callback<MyPageProfileResponse>{
            override fun onResponse(call: Call<MyPageProfileResponse>, response: Response<MyPageProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val myPageResponse = response.body()
                    Log.e("MyPageFragment response body", "$myPageResponse")
                    Log.e("MyPageFragment response code", "${response.code()}")

                    if (myPageResponse != null) {
                        binding.myPageNickname.text = myPageResponse.data.nickname
                        binding.myPageAddress.text = myPageResponse.data.address
                        binding.myPageField1.text = myPageResponse.data.fieldList[0]
                        binding.myPageField2.text = myPageResponse.data.fieldList[1]


                    }
                }
            }

            override fun onFailure(call: Call<MyPageProfileResponse>, t: Throwable) {
                Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
            }

        })

        return binding.root
    }
}