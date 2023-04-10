package com.example.coding_study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coding_study.databinding.StudyFragmentBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StudyFragment : Fragment(R.layout.study_fragment) {
    private lateinit var viewModel: StudyViewModel
    private lateinit var studyAdapter: StudyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 뷰모델 인스턴스 가져오기
        viewModel = ViewModelProvider(requireActivity()).get(StudyViewModel::class.java)
        // 게시글 목록 뷰 생성
        val view = inflater.inflate(R.layout.study_fragment, container, false)
        val binding = StudyFragmentBinding.bind(view)
        val postRecyclerView = binding.studyRecyclerView

        // 어댑터 설정
        studyAdapter = StudyAdapter(listOf())
        postRecyclerView.adapter = studyAdapter
        binding.studyRecyclerView.layoutManager =
            LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정

        binding.floatingActionButton.setOnClickListener { // +버튼 (글쓰기 버튼) 눌렀을 때
            val studyuploadFragment = StudyUpload() // StudyUploadFragment로 변경
            childFragmentManager.beginTransaction()
                .add(R.id.study_fragment_layout, studyuploadFragment, "STUDY_FRAGMENT")
                .addToBackStack("STUDY_FRAGMENT")
                .commit()

        }


        // 뷰모델에서 게시글 데이터를 가져와서 어댑터에 설정
        viewModel.postList.observe(viewLifecycleOwner) { postList -> // postList 변수가 업데이트 될 때마다
            studyAdapter.postList = postList // 어댑터의 postList 변수 업데이트
            studyAdapter.notifyDataSetChanged() // notifyDataSetChanged() 메서드를 호출하여 변경 내용을 화면에 반영
        }


        return view
    }
}
