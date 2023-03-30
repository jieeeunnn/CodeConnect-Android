package com.example.coding_study

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.coding_study.databinding.JoinFragmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.navigation.fragment.NavHostFragment



class JoinFragment : Fragment(R.layout.join_fragment) {
    private lateinit var binding: JoinFragmentBinding
    private var field: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = JoinFragmentBinding.inflate(inflater, container, false)


        val retrofit = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8081/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val joinService = retrofit.create(JoinService::class.java)


        binding.textAddress.setOnClickListener {
            val joinaddressFragment = AddressFragment()

            childFragmentManager.beginTransaction()
                .add(R.id.join_fragment, joinaddressFragment, "JOIN_FRAGMENT")
                .addToBackStack("JOIN_FRAGMENT")
                .commit()

        }

/*
        binding.textAddress.setOnClickListener {
            val navController = Navigation.findNavController(view)
            //val navController = findNavController()
            navController.navigate(R.id.action_joinFragment_to_addressFragment)
        }

 */



        binding.buttonAndroid.setOnClickListener {
            field = "ANDROID"
        }

        binding.buttonIos.setOnClickListener {
            field = "IOS"
        }

        binding.buttonAlgorithm.setOnClickListener {
            field = "ALGORITHM"
        }

        binding.buttonDatabase.setOnClickListener {
            field = "DATABASE"
        }

        binding.buttonOs.setOnClickListener {
            field = "OS"
        }

        binding.buttonServer.setOnClickListener {
            field = "SERVER"
        }

        binding.buttonWeb.setOnClickListener {
            field = "WEB"
        }

        binding.buttonMachine.setOnClickListener {
            field = "MACHINE_LEARNING"
        }

        binding.buttonEtc.setOnClickListener {
            field = "ETC"
        }

        binding.joinButton2.setOnClickListener{
            val nickname = binding.editNickname.text.toString()
            val email = binding.editEmailAddress.text.toString()
            val password = binding.editTextPassword.text.toString()
            val passwordCheck = binding.editPwdCheck.text.toString()
            val state = binding.editAddress1.text.toString()
            val city = binding.editAddress2.text.toString()

            val joinRequest = JoinRequest(email, password, passwordCheck, nickname, state, city, field.toString())

            Log.e("Login", "email: $email, password: $password, passwordCheck: $passwordCheck, " +
                    "nickname: $nickname, state: $state, city: $city, field: $field")


            joinService.requestJoin(joinRequest).enqueue(object: Callback<JoinResponse> {
                override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) { // 통신에 성공했을 때

                    val code = response.code() // 서버 응답 코드
                    Log.e("response code", "is : $code")

                    if (response.isSuccessful) {
                        val joinResponse = response.body() // 서버에서 받아온 응답 데이터

                        Log.e("Join", "is : ${response.body()}")

                        if (joinResponse?.result == true && joinResponse.data != null) {
                            val nextIntent = Intent(requireActivity(), SecondActivity::class.java)
                            startActivity(nextIntent)

                            //val nextIntent = Intent(this@JoinFragment, SecondActivity::class.java)
                            //startActivity(nextIntent) // SecondActivity (스터디 게시글 화면) 창으로 이동
                        }
                    } else {
                        //서버로부터 응답이 실패한 경우
                        JoinDialogFragment().show(childFragmentManager, "JoinDialogFragment")

                        //JoinDialogFragment().show(supportFragmentManager,"JoinDialogFragment")
                    }
                }

                override fun onFailure(call: Call<JoinResponse>, t: Throwable) { // 통신에 실패했을 때
                    ErrorDialogFragment().show(childFragmentManager, "Join_ErrorDialogFragment")


                    //ErrorDialogFragment().show(supportFragmentManager, "Join_ErrorDialogFragment")
                }
            })
        }
        print("onCreateView")
        return binding.root
    }
}
