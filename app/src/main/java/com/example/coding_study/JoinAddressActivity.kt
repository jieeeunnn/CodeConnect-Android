package com.example.coding_study

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import com.example.coding_study.databinding.ActivityJoinAddressBinding
class JoinAddressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinAddressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SearchView 설정
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색 버튼을 눌렀을 때 처리할 내용
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어가 변경될 때 처리할 내용
                return false
            }
        })
    }
}