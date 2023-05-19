package com.example.coding_study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.coding_study.databinding.MypageEditBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class MyPageEditFragment:Fragment(R.layout.mypage_edit) {
    private lateinit var binding: MypageEditBinding
    private var selectedFields = mutableListOf<String>() // selectedFields 리스트 정의
    private lateinit var addressViewModel: MyPageAddressViewModel

    private fun updateSelectedFields(field: String) { // 클릭된 버튼을 selectedFields 리스트에 추가하는 함수 (2개 선택)
        if (selectedFields.contains(field)) { // 이미 선택된 상태였을 경우
            selectedFields.remove(field) // 필드값 삭제
        } else {
            if (selectedFields.size < 2) { // 선택되지 않은 상태였을 경우, selectedFields 리스트에 필드값이 2개 미만이면 리스트에 필드값 추가
                selectedFields.add(field)
            }
        }
    }

    private fun updateButtonAppearance(button: Button, isSelected: Boolean) {  // field 버튼이 눌리면 색상 변경하는 함수
        if (isSelected) {
            button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue_sky)
        } else {
            button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.blue_main)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MypageEditBinding.inflate(inflater, container, false)
        val myPageViewModel: MyPageViewModel by viewModels({ requireParentFragment() })
        val myProfile: MyProfile? = myPageViewModel.getMyProfile().value

        addressViewModel = ViewModelProvider(requireActivity()).get(MyPageAddressViewModel::class.java)


        if (myProfile != null) {
            binding.myPageNewId.setText(myProfile.nickname)
            binding.myPageNewAddress.text = myProfile.address

            binding.myPageNewAddress.setOnClickListener {
                val addressFragment = MyPageAddressFragment()

                childFragmentManager.beginTransaction()
                    .replace(R.id.myPageEdit_layout, addressFragment)
                    .addToBackStack(null)
                    .commit()
            }

            addressViewModel.getSelectedAddress().observe(viewLifecycleOwner) {
                address ->
                if (address == "") {
                    binding.myPageNewAddress.text = myProfile.address
                } else {
                    binding.myPageNewAddress.text = address

                }

                Log.e("MyPageEditFragment", "selected address: $address")
            }

            val fieldList: List<String> = myProfile.fieldList

            // 필드와 버튼 ID 간의 매핑을 위한 HashMap 생성
            val fieldButtonMap = hashMapOf(
                "안드로이드" to binding.myPageAndroidBtn,
                "ios" to binding.myPageIosBtn,
                "알고리즘" to binding.myPageAlgorithmBtn,
                "데이터베이스" to binding.myPageDatabaseBtn,
                "운영체제" to binding.myPageOsBtn,
                "서버" to binding.myPageServerBtn,
                "웹" to binding.myPageWebBtn,
                "머신러닝" to binding.myPageMachinBtn,
                "기타" to binding.myPageEtcBtn
            )

            // 필드 리스트에 있는 모든 필드에 대해 버튼 색상 업데이트
            for (field in fieldList) {
                if (fieldButtonMap.containsKey(field)) {
                    val button = fieldButtonMap[field]
                    if (button != null) {
                        updateSelectedFields(field)
                        updateButtonAppearance(button, true)
                    }
                }
            }
        }

        binding.myPageAndroidBtn.setOnClickListener {
            updateSelectedFields("안드로이드")
            updateButtonAppearance(binding.myPageAndroidBtn, selectedFields.contains("안드로이드"))
        }

        binding.myPageIosBtn.setOnClickListener {
            updateSelectedFields("ios")
            updateButtonAppearance(binding.myPageIosBtn, selectedFields.contains("ios"))
        }

        binding.myPageAlgorithmBtn.setOnClickListener {
            updateSelectedFields("알고리즘")
            updateButtonAppearance(binding.myPageAlgorithmBtn, selectedFields.contains("알고리즘"))
        }

        binding.myPageDatabaseBtn.setOnClickListener {
            updateSelectedFields("데이터베이스")
            updateButtonAppearance(binding.myPageDatabaseBtn, selectedFields.contains("데이터베이스"))
        }

        binding.myPageOsBtn.setOnClickListener {
            updateSelectedFields("운영체제")
            updateButtonAppearance(binding.myPageOsBtn, selectedFields.contains("운영체제"))
        }

        binding.myPageServerBtn.setOnClickListener {
            updateSelectedFields("서버")
            updateButtonAppearance(binding.myPageServerBtn, selectedFields.contains("서버"))
        }

        binding.myPageWebBtn.setOnClickListener {
            updateSelectedFields("웹")
            updateButtonAppearance(binding.myPageWebBtn, selectedFields.contains("웹"))
        }

        binding.myPageMachinBtn.setOnClickListener {
            updateSelectedFields("머신러닝")
            updateButtonAppearance(binding.myPageMachinBtn, selectedFields.contains("머신러닝"))
        }

        binding.myPageEtcBtn.setOnClickListener {
            updateSelectedFields("기타")
            updateButtonAppearance(binding.myPageEtcBtn, selectedFields.contains("기타"))
        }

        val sharedPreferences =
            requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token.orEmpty())
                            //.addHeader("Authorization", "Bearer $token")
                            .build()
                        Log.d("TokenInterceptor_StudyFragment", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val myPageEditService = retrofitBearer.create(MyPageEditService::class.java)

        binding.myPageUploadButton.setOnClickListener {
            if (selectedFields.size < 2) { // 필드를 2개 선택하지 않았을 경우 Toast 메세지 띄우기
                val confirmDialog = ConfirmDialog("관심 언어 2개를 선택해 주세요")
                confirmDialog.isCancelable = false
                confirmDialog.show(childFragmentManager, "joinFragment joinButton2_관심사 선택")
            }
            else{
                val nickname = binding.myPageNewId.text.toString()
                val address = binding.myPageNewAddress.text.toString()
                val fieldList = selectedFields.toList()

                var myPageEdit = MyPageEdit(nickname, address, fieldList)

                myPageEditService.myPageEditPost(myPageEdit).enqueue(object : Callback<MyPageEditResponse> {
                    override fun onResponse(
                        call: Call<MyPageEditResponse>,
                        response: Response<MyPageEditResponse>
                    ) {
                        if (response.isSuccessful) {
                            Log.e("MyPageEditFragment response code is", "${response.code()}")
                            Log.e("MyPageEditFragment response body is", "${response.body()}")

                            val parentFragmentManager = requireActivity().supportFragmentManager
                            parentFragmentManager.popBackStack()
                        }
                        else {
                            Log.e("MyPageEditFragment_onResponse","But not success")

                        }
                    }

                    override fun onFailure(call: Call<MyPageEditResponse>, t: Throwable) {
                        Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                    }

                })
            }
        }

        return binding.root
    }
}