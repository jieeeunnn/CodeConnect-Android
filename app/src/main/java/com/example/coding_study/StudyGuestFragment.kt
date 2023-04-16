package com.example.coding_study

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.StudyGuestBinding
import com.google.gson.Gson

class GuestJoinFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle("스터디 참여 신청")
            setMessage("스터디 참여 신청이 완료되었습니다")
            setPositiveButton("확인") {dialog, id -> println("스터디 참여 신청 확인")}
        }.create()
    }
}

class StudyGuestFragment : Fragment(R.layout.study_guest) {
    private lateinit var binding: StudyGuestBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StudyGuestBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 가져온 recruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val gson = Gson()
        val json = arguments?.getString("recruitmentJson")
        val recruitment = gson.fromJson(json, RecruitmentDto::class.java)

        Log.e("StudyGuestFragment","$recruitment")

        // recruitment 변수에서 게시글 정보를 가져와서 레이아웃에 표시
        binding.guestNicknameText.text = recruitment.nickname
        binding.guestTitleText.text = recruitment.title
        binding.guestContentText.text = recruitment.content
        binding.guestFieldText.text = recruitment.field
        binding.guestCountText.text = recruitment.count.toString()
        binding.guestCurrentText.text = recruitment.currentDateTime.substring(0, 10)


        binding.guestButton.setOnClickListener { // 참여하기 버튼을 누를 시
            GuestJoinFragment().show(childFragmentManager, "GuestJoin Dialog")
            // 이미 신청한 스터디를 또 신청하기 버튼을 누르면 어떻게 처리할 것인가?

        }
    }
}