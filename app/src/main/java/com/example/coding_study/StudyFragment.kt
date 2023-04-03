package com.example.coding_study

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.StudyFragmentBinding

class StudyFragment : Fragment(R.layout.study_fragment) {  //스터디 게시판 fragment

    private val viewModel by viewModels<StudyViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = StudyFragmentBinding.bind(view)

        val adapter = StudyAdapter(viewModel.postList.value ?: emptyList()) // StudyAdapter 객체를 만듬
        binding.studyRecyclerView.adapter = adapter // recyclerView와 StudyAdapter 연결
        binding.studyRecyclerView.layoutManager = LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정

        binding.floatingActionButton.setOnClickListener { // +버튼 (글쓰기 버튼) 눌렀을 때
            StudyUpload().show(childFragmentManager, "studyUpload")
        }

        // 게시글 목록 관찰하여 어댑터 갱신
        viewModel.postList.observe(viewLifecycleOwner) { posts ->
            if (posts != null) {
                adapter.updatePosts(posts)
            }
        }
/*
        viewModel.itemClickEvent.observe(viewLifecycleOwner) { position ->
            StudyUpload(position).show(childFragmentManager, "studyUpload") // 클릭한 게시글의 인덱스 정보를 함께 전달하여 StudyUpload DialogFragment를 호출
        }

        val adapter = StudyAdapter(viewModel) // StudyAdapter 객체를 만듬
        binding.studyRecyclerView.adapter = adapter // recyclerView와 StudyAdapter 연결
        binding.studyRecyclerView.layoutManager = LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정

        viewModel.itemsLiveData.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged() // 데이터 전체가 바뀌었음을 알려줌
        }
 */
    }
}