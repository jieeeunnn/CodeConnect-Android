package com.example.coding_study

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.StudyFragmentBinding

class StudyFragment : Fragment(R.layout.study_fragment) {  //스터디 게시판 fragment

    private val viewModel by viewModels<StudyViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = StudyFragmentBinding.bind(view)
        val adapter = StudyAdapter(viewModel)

        //val adapter = StudyAdapter(viewModel.postList.value ?: emptyList()) // StudyAdapter 객체를 만듬
        binding.studyRecyclerView.adapter = adapter // recyclerView와 StudyAdapter 연결
        binding.studyRecyclerView.layoutManager = LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정

        binding.floatingActionButton.setOnClickListener { // +버튼 (글쓰기 버튼) 눌렀을 때
            //StudyUpload().show(childFragmentManager, "studyUpload")
            val studyuploadFragment = StudyUpload()
            childFragmentManager.beginTransaction()
                .add(R.id.study_fragment_layout, studyuploadFragment, "STUDY_FRAGMENT")
                .addToBackStack("STUDY_FRAGMENT")
                .commit()
        }
/*
        // 새로운 게시글 추가 이벤트 구독
        viewModel.onPostAdded.observe(viewLifecycleOwner) { newPost ->
            Log.e("StudyFragment_onPostAdded", "newPost: $newPost")
            // 새로운 게시글이 추가되었을 때 호출될 함수
            if (newPost != null) { // onPostAdded가 초기에 null인 경우 처리
                adapter.addPost(newPost)
            }
        }
 */

        viewModel.onPostAdded.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { newPost ->
                //Log.e("StudyFragment_onPostAdded", "newPost: $newPost")
                // 새로운 게시글이 추가되었을 때 호출될 함수
                adapter.addPost(newPost)
                Log.e("StudyFragment_onPostAdded", "newPost: $newPost")
            }
        }

        // 게시글 목록 관찰하여 어댑터 갱신
        viewModel.postList.observe(viewLifecycleOwner) { posts ->
            Log.d("StudyFragment_postList", "New post added: $posts")
            if (posts != null) {
                adapter.updatePosts(posts)
            }
        }
/*
        viewModel.postAdded.observe(viewLifecycleOwner) {
            viewModel.postList.value?.lastOrNull()?.let { adapter.addPost(it) }
        }

 */

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