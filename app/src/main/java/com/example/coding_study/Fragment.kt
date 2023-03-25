package com.example.coding_study

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog.show
import android.os.Bundle
import android.view.View
import com.example.coding_study.databinding.*
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlin.math.log

class LoginDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle("로그인 실패")
            setMessage("아이디, 비밀번호를 확인하세요")
            setPositiveButton("확인") {dialog, id -> println("LoginDialogFragment 확인")}
        }.create()
    }
}

class ErrorDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle("서버 연결 실패")
            setMessage("서버 연결을 확인하세요")
            setPositiveButton("확인") {dialog, id -> println("ErrorDialogFragment 확인")}
        }.create()
    }
}

class JoinDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext()).apply {
            setTitle("회원가입 실패")
            setMessage("모든 값을 입력했는지 확인하세요")
            setPositiveButton("확인") {dialog, id -> println("JoinDialogFragment 확인")}
        }.create()
    }
}


class StudyFragment : Fragment(R.layout.study_fragment) {  //스터디 게시판 fragment

    private val viewModel by viewModels<MyViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = StudyFragmentBinding.bind(view)
        binding.floatingActionButton.setOnClickListener { // +버튼 (글쓰기 버튼) 눌렀을 때
            StudyUpload().show(childFragmentManager, "studyUpload")
        //findNavController().navigate(R.id.action_studyFragment_to_studyUpload) // studyUpload fragment로 이동
        }

        val adapter = StudyAdapter(viewModel) // StudyAdapter 객체를 만듬
        binding.recyclerView.adapter = adapter // recyclerView와 StudyAdapter 연결
        binding.recyclerView.layoutManager = LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정

        viewModel.itemsLiveData.observe(this) {
            adapter.notifyDataSetChanged() // 데이터 전체가 바뀌었음을 알려줌
        }

    }
}

class QnAFragment : Fragment(R.layout.qna_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //val binding = QnaFragmentBinding.bind(view)

    }
}

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

class StudyUpload(val clickedItemPos: Int = -1) : DialogFragment() { // study 게시판 글쓰기 fragment
    private lateinit var binding: WriteStudyBinding

    private val viewModel by activityViewModels<MyViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WriteStudyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // 클릭했을 때 아래로 펼쳐지는 레이아웃
        binding.spinner.adapter = adapter // spinner와 adapter를 연결하여 spinner는 ArrayAdapter 안에 있는 filters 중에서 선택 가능

        if (clickedItemPos >= 0) { // 생성자 인자로 받은 clickedItemPos가 0보다 크면 해당 데이터를 찾아서 위젯 내용 초기화
            val f = viewModel.items[clickedItemPos].filter
            val s = filters.indexOf(f)
            binding.spinner.setSelection(s) // 스피너 선택 값 초기화

            binding.editTitle.setText(viewModel.items[clickedItemPos].title)
            binding.editNumber.setText(viewModel.items[clickedItemPos].num)
            binding.editContent.setText(viewModel.items[clickedItemPos].content)

        } else {
            binding.spinner.setSelection(0)
        }

        binding.spinner.setSelection(0)

        binding.buttonUpload.setOnClickListener{
            val filter = binding.spinner.selectedItem as String // 스피너 선택 값 가져오기
            val id = "hansung"
            val item = Item(id, binding.editTitle.text.toString(),
                binding.editContent.text.toString(), filter, binding.editNumber.text.toString())

            if (clickedItemPos < 0) {
                viewModel.addItem(item)
                Log.v("tag", "uploadButton_addItem")
                }
            else
                viewModel.updateItem(item, clickedItemPos)

        }

    }
    companion object {
        val filters = arrayListOf("안드로이드", "ios", "알고리즘", "데이터베이스", "운영체제", "서버", "웹", "머신러닝", "기타")
    }
}