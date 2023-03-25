package com.example.coding_study

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.coding_study.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {
    val binding by lazy {ActivityMainBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:8080/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val loginService = retrofit.create(LoginService::class.java)


        //로그인 버튼 누를 때
        binding.logButton.setOnClickListener {
            val email = binding.editEmail.toString()
            val password = binding.editPassword.toString()


            loginService.requestLogin(email, password).enqueue(object: Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) { // 통신에 성공했을 때
                    if (response.isSuccessful) {
                        val loginResponse = response.body() // 서버에서 받아온 응답 데이터
                        if (loginResponse?.success == true && loginResponse.user != null) {
                            val nextIntent = Intent(this@MainActivity, SecondActivity::class.java)
                            startActivity(nextIntent)
                        }
                    } else {
                        // 서버로부터 응답이 실패한 경우
                        LoginDialogFragment().show(supportFragmentManager,"")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) { // 통신에 실패했을 때
                    ErrorDialogFragment().show(supportFragmentManager,"") // 로그인 실패 다이얼로그 띄우기
                }
            })
        }


        //회원가입 버튼을 누를 때
        binding.joinButton.setOnClickListener{
            val nextIntent = Intent(this@MainActivity, JoinActivity::class.java) // 회원가입(JoinActivity) 창으로 이동
            startActivity(nextIntent)

        }
    }
}