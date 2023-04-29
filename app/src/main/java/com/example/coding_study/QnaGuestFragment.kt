package com.example.coding_study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.QnaGuestBinding
import com.google.gson.Gson

class QnaGuestFragment : Fragment(R.layout.qna_guest) {
    private lateinit var binding: QnaGuestBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QnaGuestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 가져온 qnaRecruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaRecruitmentJson")
        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)

        binding.qnaGuestNickname.text = qnaRecruitment.nickname
        binding.qnaGuestTitle.text = qnaRecruitment.title
        binding.qnaGuestContent.text = qnaRecruitment.content
        binding.qnaGuestCurrentTime.text = qnaRecruitment.currentDateTime

    }
}