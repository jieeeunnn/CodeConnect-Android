package com.example.coding_study

import android.os.Bundle
import android.view.View
import com.example.coding_study.databinding.*
import androidx.fragment.app.*


class ChatFragment : Fragment(R.layout.chat_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ChatFragmentBinding.bind(view)

    }
}

class MypageFragment : Fragment(R.layout.mypage_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MypageFragmentBinding.bind(view)
    }
}

class WriteStudyFragment : Fragment(R.layout.write_study) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = WriteStudyBinding.bind(view)
    }
}