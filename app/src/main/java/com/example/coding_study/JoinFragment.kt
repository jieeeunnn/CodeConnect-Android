package com.example.coding_study

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.coding_study.databinding.JoinFragmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class JoinFragment : Fragment(R.layout.join_fragment) {
    private lateinit var binding: JoinFragmentBinding
    private var field: String? = null
    private lateinit var viewModel: AddressViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = JoinFragmentBinding.inflate(inflater, container, false)

        val activity = requireActivity() as MainActivity
        activity.hideButton()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()

            val activity = requireActivity() as MainActivity
            activity.showButton()
        }

        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity()).get(AddressViewModel::class.java)

        // 가져온 데이터를(AddressFragment에서 선택한 주소) 사용해서 textViewAddress1 업데이트
        viewModel.getSelectedAddress().observe(viewLifecycleOwner) { address ->
            binding.textViewAddress1.text = address

            Log.e("JoinFragment", "Selected address: $address") // 선택된 address 변수 값 로그 출력
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val joinService = retrofit.create(JoinService::class.java)

        binding.textViewAddress1.setOnClickListener { // textViewAddress1을 클릭하면 주소 검색 창으로 이동
            val addressFragment = AddressFragment()

            childFragmentManager.beginTransaction()
                .replace(R.id.join_fragment, addressFragment, "JOIN_FRAGMENT")
                .addToBackStack("JOIN_FRAGMENT")
                .commit()
        }

        val selectedFields = mutableListOf<String>() // selectedFields 리스트 정의

        fun updateSelectedFields(field: String) { // 클릭된 버튼을 selectedFields 리스트에 추가하는 함수 (2개 선택)
            if (selectedFields.contains(field)) { // 이미 선택된 상태였을 경우
                selectedFields.remove(field) // 필드값 삭제
            } else {
                if (selectedFields.size < 2) { // 선택되지 않은 상태였을 경우, selectedFields 리스트에 필드값이 2개 미만이면 리스트에 필드값 추가
                    selectedFields.add(field)
                }
            }
        }

        fun updateButtonAppearance(button: Button, isSelected: Boolean) {  // field 버튼이 눌리면 색상 변경하는 함수
            if (isSelected) {
                button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue_sky)
            } else {
                button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue_main)
            }
        }

        binding.buttonAndroid.setOnClickListener {
            updateSelectedFields("안드로이드")
            updateButtonAppearance(binding.buttonAndroid, selectedFields.contains("안드로이드"))
        }

        binding.buttonIos.setOnClickListener {
            updateSelectedFields("ios")
            updateButtonAppearance(binding.buttonIos, selectedFields.contains("ios"))
        }

        binding.buttonAlgorithm.setOnClickListener {
            updateSelectedFields("알고리즘")
            updateButtonAppearance(binding.buttonAlgorithm, selectedFields.contains("알고리즘"))
        }

        binding.buttonDatabase.setOnClickListener {
            updateSelectedFields("데이터베이스")
            updateButtonAppearance(binding.buttonDatabase, selectedFields.contains("데이터베이스"))
        }

        binding.buttonOs.setOnClickListener {
            updateSelectedFields("운영체제")
            updateButtonAppearance(binding.buttonOs, selectedFields.contains("운영체제"))
        }

        binding.buttonServer.setOnClickListener {
            updateSelectedFields("서버")
            updateButtonAppearance(binding.buttonServer, selectedFields.contains("서버"))
        }

        binding.buttonWeb.setOnClickListener {
            updateSelectedFields("웹")
            updateButtonAppearance(binding.buttonWeb, selectedFields.contains("웹"))
        }

        binding.buttonMachine.setOnClickListener {
            updateSelectedFields("머신러닝")
            updateButtonAppearance(binding.buttonMachine, selectedFields.contains("머신러닝"))
        }

        binding.buttonEtc.setOnClickListener {
            updateSelectedFields("기타")
            updateButtonAppearance(binding.buttonEtc, selectedFields.contains("기타"))
        }


        binding.joinButton2.setOnClickListener {
            if (selectedFields.size < 2) { // 필드를 2개 선택하지 않았을 경우 Toast 메세지 띄우기
                val cofirmDialog = ConfirmDialog("관심사 2개를 선택해 주세요")
                cofirmDialog.isCancelable = false
                cofirmDialog.show(childFragmentManager, "joinFragment joinButton2_관심사 선택")
            } else {
                val nickname = binding.editNickname.text.toString()
                val email = binding.editEmailAddress.text.toString()
                val password = binding.editTextPassword.text.toString()
                val passwordCheck = binding.editPwdCheck.text.toString()
                val address = binding.textViewAddress1.text.toString()
                val fieldList = selectedFields.toList()

                val joinRequest = JoinRequest(email, password, passwordCheck, nickname, address, fieldList)

                Log.e("JoinFragment_joinRequest", "email: $email, password: $password, passwordCheck: $passwordCheck, " +
                            "nickname: $nickname, address:$address, fieldList: $fieldList")


                joinService.requestJoin(joinRequest).enqueue(object : Callback<JoinResponse> {
                    override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) { // 통신에 성공했을 때

                        val code = response.code() // 서버 응답 코드
                        Log.e("response code", "is : $code")

                        if (response.isSuccessful) {
                            val joinResponse = response.body() // 서버에서 받아온 응답 데이터

                            Log.e("Join", "is : ${response.body()}")

                            if (joinResponse?.result == true && joinResponse.data != null) {
                                val nextIntent = Intent(requireActivity(), MainActivity::class.java)
                                startActivity(nextIntent)

                                //val nextIntent = Intent(this@JoinFragment, SecondActivity::class.java)
                                //startActivity(nextIntent) // SecondActivity (스터디 게시글 화면) 창으로 이동
                            }
                        } else {
                            //서버로부터 응답이 실패한 경우
                            val cofirmDialog = ConfirmDialog("모든 값을 입력했는지 확인하세요")
                            cofirmDialog.isCancelable = false
                            cofirmDialog.show(childFragmentManager, "JoinFragment joinButton2")
                        }
                    }

                    override fun onFailure(call: Call<JoinResponse>, t: Throwable) { // 통신에 실패했을 때
                        Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
        return binding.root
    }
}
