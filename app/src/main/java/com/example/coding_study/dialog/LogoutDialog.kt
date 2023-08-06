package com.example.coding_study.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.coding_study.common.MainActivity
import com.example.coding_study.common.TokenManager
import com.example.coding_study.databinding.DeleteDialogBinding
import com.example.coding_study.mypage.LogoutResponse
import com.example.coding_study.mypage.MemberLogout
import com.example.coding_study.mypage.MyPageLogoutService
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LogoutDialog: DialogFragment() {
    private lateinit var binding: DeleteDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DeleteDialogBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명
        binding.dialogTextView.text = "로그아웃 하시겠습니까?"

        binding.dialogNoButton.setOnClickListener {
            dismiss()
        }

        binding.dialogYesButton.setOnClickListener {
            val tokenManager = context?.let { it1 -> TokenManager(it1) }
            val token = tokenManager?.getAccessToken()

            val retrofitBearer = Retrofit.Builder()
                .baseUrl("http://52.79.53.62:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor { chain ->
                            val request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + token.orEmpty())
                                .build()
                            Log.d("TokenInterceptor_memberLogout", "Token: " + token.orEmpty())
                            chain.proceed(request)
                        }
                        .build()
                )
                .build()

            val logoutService = retrofitBearer.create(MyPageLogoutService::class.java)
            val memberLogout = token?.let { it1 -> MemberLogout(it1) }

            if (memberLogout != null) {
                logoutService.memberLogout(memberLogout).enqueue(object : Callback<LogoutResponse> {
                    override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                        Log.e("Mypage Logout response code", "${response.code()}")
                        if (token != null) {
                            Log.e("Mypage logout token", memberLogout.toString())
                        }

                        if (response.isSuccessful) {
                            val logoutResponse = response.body()
                            Log.e("Mypage Logout response body", "$logoutResponse")

                            if (logoutResponse != null) {
                                if (logoutResponse.result == true) {
                                    dismiss()

                                    // MainActivity로 이동
                                    val intent = Intent(requireContext(), MainActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    dismiss()
                                    Toast.makeText(context, "로그아웃에 실패했습니다", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                        Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
        return binding.root
    }
}