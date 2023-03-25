package com.example.coding_study

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.coding_study.databinding.ActivityJoinBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JoinActivity : AppCompatActivity() {
    private var field: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val joinService = retrofit.create(JoinService::class.java)

        binding.buttonAndroid.setOnClickListener{
            field = "ANDROID"
        }
        binding.buttonIos.setOnClickListener{
            field = "IOS"
        }
        binding.buttonAlgorithm.setOnClickListener{
            field = "ALGORITHM"
        }
        binding.buttonDatabase.setOnClickListener{
            field = "DATABASE"
        }
        binding.buttonOs.setOnClickListener{
            field = "OS"
        }
        binding.buttonServer.setOnClickListener{
            field = "SERVER"
        }
        binding.buttonWeb.setOnClickListener{
            field = "WEB"
        }
        binding.buttonMachine.setOnClickListener{
            field = "MACHINE_LEARNING"
        }
        binding.buttonEtc.setOnClickListener{
            field = "ETC"
        }


        // 가입하기 버튼을 누를 때
        binding.joinButton2.setOnClickListener{
            val nickname = binding.editNickname.text.toString()
            val email = binding.editEmailAddress.text.toString()
            val password = binding.editTextPassword.text.toString()
            val address1 = binding.editAddress1.text.toString()
            val address2 = binding.editAddress2.text.toString()

            val joinRequest = JoinRequest(email, password, nickname, address1, address2, field.toString())

            Log.e("Login", "email: $email, password: $password, nickname: $nickname, address1: $address1, address2: $address2, field: $field")


            joinService.requestJoin(joinRequest).enqueue(object: Callback<JoinResponse> {
                override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) { // 통신에 성공했을 때
                    if (response.isSuccessful) {
                        val joinResponse = response.body() // 서버에서 받아온 응답 데이터
                        val code = response.code()
                        Log.e("Join", "is : ${response.body()}")
                        Log.e("response code", "is : $code")
                        if (joinResponse?.result == true && joinResponse.data != null) {
                            val nextIntent = Intent(this@JoinActivity, SecondActivity::class.java)
                            startActivity(nextIntent) // SecondActivity (스터디 게시글 화면) 창으로 이동
                        }
                    } else {
                        //서버로부터 응답이 실패한 경우
                        JoinDialogFragment().show(supportFragmentManager,"JoinDialogFragment")
                    }
                }

                override fun onFailure(call: Call<JoinResponse>, t: Throwable) { // 통신에 실패했을 때
                    ErrorDialogFragment().show(supportFragmentManager, "Join_ErrorDialogFragment")
                }
            })
        }
    }


}