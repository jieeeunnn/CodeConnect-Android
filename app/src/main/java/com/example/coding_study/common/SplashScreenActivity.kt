package com.example.coding_study.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(this@SplashScreenActivity)
        val userToken = tokenManager.getRefreshToken()

        if (userToken != null) { // not null이 아니라 시간 유효로 바꾸기
            Log.e("SplashScreenActivity userToken", userToken)
            startStudyFragment()
        } else {
            startMainActivity()
        }
    }

    private fun startStudyFragment() { // 스터디 게시판으로 이동
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티를 종료하여 스택에서 제거
    }

    private fun startMainActivity() { // 로그인 화면으로 이동
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티를 종료하여 스택에서 제거
    }

}