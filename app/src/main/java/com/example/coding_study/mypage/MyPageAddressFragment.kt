package com.example.coding_study.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.*
import com.example.coding_study.common.AddressAdapter
import com.example.coding_study.common.AddressApiService
import com.example.coding_study.common.Feature
import com.example.coding_study.common.Welcome7
import com.example.coding_study.databinding.AddressFragmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyPageAddressFragment: Fragment(R.layout.address_fragment) {
    private lateinit var binding: AddressFragmentBinding
    private lateinit var viewModel: MyPageAddressViewModel
    private var mAddress: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AddressFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(MyPageAddressViewModel::class.java)

        val recyclerView = binding.addressRecyclerView
        val itemDecoration = AddressAdapter.MyItemDecoration(30, 30) // 아이템 간 간격 설정
        recyclerView.addItemDecoration(itemDecoration)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://api.vworld.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val addressService = retrofit.create(AddressApiService::class.java)
        val addressList = listOf<Feature>()

        val addressAdapter = AddressAdapter(addressList, object : AddressAdapter.ItemClickListener {
            override fun onItemClick(fullNm: String) {
                mAddress = fullNm // 멤버 변수에 선택한 주소 저장
            }
        })

        binding.OkButton.setOnClickListener {
            // ViewModel에 데이터 저장
            viewModel.selectAddress(mAddress)
            parentFragmentManager.popBackStack()
        }
        binding.addressRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색 버튼을 눌렀을 때 처리할 로직 작성
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어가 변경될 때마다 처리할 로직 작성
                addressService.getAddressList(
                    service = "data",
                    request = "GetFeature",
                    data = "LT_C_ADEMD_INFO",
                    key = "8E78F586-DBB3-36C9-9FF5-E7B652FBA77D",
                    format = "json",
                    geometry = "false",
                    attrFilter = "emd_kor_nm:like:${newText}"
                ).enqueue(object : Callback<Welcome7> {
                    override fun onResponse(call: Call<Welcome7>, response: Response<Welcome7>) {

                        //Log.e("Login", "address: ${URLEncoder.encode(query, "UTF-8")}") // 내가 보낸 data Log 출력
                        Log.e("Address", "address: $newText")

                        if (response.isSuccessful) {
                            val code = response.code() // 서버 응답 코드
                            Log.e("AddressApi response code", "is : $code")
                            Log.e("AddressApi response body", "is : ${response.body()?.response?.result?.featureCollection?.features}") // 서버에서 받아온 응답 데이터 log 출력
                            var addressList = response.body()?.response?.result?.featureCollection?.features ?: emptyList()
                            addressAdapter.submitList(addressList)
                        }
                    }

                    override fun onFailure(call: Call<Welcome7>, t: Throwable) {
                        Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                    }
                })
                return false
            }
        })
        binding.addressRecyclerView.adapter = addressAdapter


        return binding.root
    }
}