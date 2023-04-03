package com.example.coding_study

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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

    fun saveNickname(context: Context, nickname: String) {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("nickname", nickname)
        editor?.apply()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = JoinFragmentBinding.inflate(inflater, container, false)

        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity()).get(AddressViewModel::class.java)

        // 가져온 데이터를(AddressFragment에서 선택한 주소) 사용해서 textViewAddress1 업데이트
        viewModel.getSelectedAddress().observe(viewLifecycleOwner) { address ->
            binding.textViewAddress1.text = address

            Log.e("JoinFragment", "Selected address: $address") // 선택된 address 변수 값 로그 출력
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val joinService = retrofit.create(JoinService::class.java)

        binding.textViewAddress1.setOnClickListener { // testViewAddress1을 클릭하면 주소 검색 창으로 이동
            val addressFragment = AddressFragment()

            childFragmentManager.beginTransaction()
                .add(R.id.join_fragment, addressFragment, "JOIN_FRAGMENT")
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
                button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.selectedButtonBackground)
            } else {
                button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.defaultButtonBackground)
            }
        }

        binding.buttonAndroid.setOnClickListener {
            updateSelectedFields("ANDROID")
            updateButtonAppearance(binding.buttonAndroid, selectedFields.contains("ANDROID"))
        }

        binding.buttonIos.setOnClickListener {
            updateSelectedFields("IOS")
            updateButtonAppearance(binding.buttonIos, selectedFields.contains("IOS"))
        }

        binding.buttonAlgorithm.setOnClickListener {
            updateSelectedFields("ALGORITHM")
            updateButtonAppearance(binding.buttonAlgorithm, selectedFields.contains("ALGORITHM"))
        }

        binding.buttonDatabase.setOnClickListener {
            updateSelectedFields("DATABASE")
            updateButtonAppearance(binding.buttonDatabase, selectedFields.contains("DATABASE"))
        }

        binding.buttonOs.setOnClickListener {
            updateSelectedFields("OS")
            updateButtonAppearance(binding.buttonOs, selectedFields.contains("OS"))
        }

        binding.buttonServer.setOnClickListener {
            updateSelectedFields("SERVER")
            updateButtonAppearance(binding.buttonServer, selectedFields.contains("SERVER"))
        }

        binding.buttonWeb.setOnClickListener {
            updateSelectedFields("WEB")
            updateButtonAppearance(binding.buttonWeb, selectedFields.contains("WEB"))
        }

        binding.buttonMachine.setOnClickListener {
            updateSelectedFields("MACHINE_LEARNING")
            updateButtonAppearance(binding.buttonMachine, selectedFields.contains("MACHINE_LEARNING"))
        }

        binding.buttonEtc.setOnClickListener {
            updateSelectedFields("ETC")
            updateButtonAppearance(binding.buttonEtc, selectedFields.contains("ETC"))
        }


        binding.joinButton2.setOnClickListener {
            if (selectedFields.size < 2) { // 필드를 2개 선택하지 않았을 경우 Toast 메세지 띄우기
                Toast.makeText(context, "관심사 2개를 선택해주세요", Toast.LENGTH_LONG).show()
            } else {
                val nickname = binding.editNickname.text.toString()
                val email = binding.editEmailAddress.text.toString()
                val password = binding.editTextPassword.text.toString()
                val passwordCheck = binding.editPwdCheck.text.toString()
                val address = binding.textViewAddress1.text.toString()
                val fieldList = selectedFields.toList()

                val joinRequest = JoinRequest(email, password, passwordCheck, nickname, address, fieldList)

                Log.e("Login", "email: $email, password: $password, passwordCheck: $passwordCheck, " +
                            "nickname: $nickname, address:$address, fieldList: $fieldList")


                joinService.requestJoin(joinRequest).enqueue(object : Callback<JoinResponse> {
                    override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) { // 통신에 성공했을 때

                        val code = response.code() // 서버 응답 코드
                        Log.e("response code", "is : $code")

                        if (response.isSuccessful) {
                            val joinResponse = response.body() // 서버에서 받아온 응답 데이터

                            Log.e("Join", "is : ${response.body()}")

                            if (joinResponse?.result == true && joinResponse.data != null) {
                                val receivedNickname = joinResponse.data!!.nickname// 토큰 저장
                                saveNickname(context!!, receivedNickname) // receivedToken이 null이 아닌 경우 'let'블록 내부에서 savedToken 함수를 호출해 token 저장

                                val nextIntent = Intent(requireActivity(), SecondActivity::class.java)
                                startActivity(nextIntent)

                                //val nextIntent = Intent(this@JoinFragment, SecondActivity::class.java)
                                //startActivity(nextIntent) // SecondActivity (스터디 게시글 화면) 창으로 이동
                            }
                        } else {
                            //서버로부터 응답이 실패한 경우
                            JoinDialogFragment().show(childFragmentManager, "JoinDialogFragment")
                        }
                    }

                    override fun onFailure(call: Call<JoinResponse>, t: Throwable) { // 통신에 실패했을 때
                        ErrorDialogFragment().show(childFragmentManager, "Join_ErrorDialogFragment")
                    }
                })
            }
        }
        return binding.root
    }
}
