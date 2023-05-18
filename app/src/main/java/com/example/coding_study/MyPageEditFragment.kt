package com.example.coding_study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.MypageEditBinding

class MyPageEditFragment:Fragment(R.layout.mypage_edit) {
    private lateinit var binding: MypageEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MypageEditBinding.inflate(inflater, container, false)


        return binding.root
    }
}