package com.example.coding_study.study

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.*
import com.example.coding_study.common.*
import com.example.coding_study.databinding.AddressFragmentBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StudyAddressFragment : Fragment(R.layout.address_fragment) {
    private lateinit var binding: AddressFragmentBinding
    private lateinit var viewModel: AddressViewModel
    private var mAddress: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트에서 사용할 레이아웃 파일을 inflate 합니다.
        binding = AddressFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(AddressViewModel::class.java)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()

            val parentFragment = parentFragment
            if (parentFragment is StudyFragment) {
                parentFragment.showFloatingButton()
            }
        }

        val recyclerView = binding.addressRecyclerView
        val itemDecoration = AddressAdapter.MyItemDecoration(30, 30) // 아이템 간 간격 설정
        recyclerView.addItemDecoration(itemDecoration)

        // Retrofit 인스턴스 생성
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

        val sharedPreferences = requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token.orEmpty())
                            .build()
                        Log.d("TokenInterceptor_StudyFragment", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val studyAddressService = retrofitBearer.create(StudyGetService::class.java)

        binding.OkButton.setOnClickListener {
            // ViewModel에 데이터 저장
            viewModel.selectAddress(mAddress)

            studyAddressService.studyGetList(address = mAddress).enqueue(object : Callback<StudyListResponse>{
                override fun onResponse(call: Call<StudyListResponse>, response: Response<StudyListResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.e("StudyAddressService response code", "${response.code()}")
                        Log.e("StudyAddressService response body", "${response.body()}")

                        parentFragmentManager.popBackStack()

                        val parentFragment = parentFragment
                        if (parentFragment is StudyFragment) {
                            parentFragment.showFloatingButton()
                        }

                    } else {
                        Log.e("StudyAddressService onResponse", "But not success")
                    }
                }
                override fun onFailure(call: Call<StudyListResponse>, t: Throwable) {
                    Toast.makeText(context, "StudyAddressService_서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
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
                    key = "D3D9E0D0-062C-35F0-A49D-FC9E863B3AD5",
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parentFragment = parentFragment
        if (parentFragment is StudyFragment) {
            parentFragment.hideFloatingButton()
        }
    }
}