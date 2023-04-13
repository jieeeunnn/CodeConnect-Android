package com.example.coding_study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.StudyHostBinding
import com.google.gson.Gson

class StudyHostFragment : Fragment(R.layout.study_host) {
    private lateinit var binding: StudyHostBinding
    //private lateinit var recruitment: RecruitmentDto

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StudyHostBinding.inflate(inflater, container, false)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 가져온 recruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val gson = Gson()
        val json = arguments?.getString("recruitmentJson")
        val recruitment = gson.fromJson(json, RecruitmentDto::class.java)

        // recruitment 변수에서 게시글 정보를 가져와서 레이아웃에 표시
        binding.hostNicknameText.text = recruitment.nickname
        binding.hostTitleText.text = recruitment.title
        binding.hostContentText.text = recruitment.content
        binding.hostFieldText.text = recruitment.field
        binding.hostCountText.text = recruitment.count.toString()
        binding.hostCurrentText.text = recruitment.currentDateTime
    }

}
