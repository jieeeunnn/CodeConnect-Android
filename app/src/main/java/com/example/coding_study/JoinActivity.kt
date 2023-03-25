package com.example.coding_study

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.coding_study.databinding.ActivityJoinBinding

class JoinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 가입하기 버튼을 누를 때
        binding.joinButton2.setOnClickListener{
            val nextIntent = Intent(this@JoinActivity, SecondActivity::class.java)
            startActivity(nextIntent) // SecondActivity (스터디 게시글 화면) 창으로 이동
        }
    }


}