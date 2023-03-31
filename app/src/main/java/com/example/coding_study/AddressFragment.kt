package com.example.coding_study

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.beust.klaxon.Klaxon
import com.example.coding_study.databinding.AddressFragmentBinding
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URLEncoder

interface AddressApiService {
    @GET("req/data")
    fun getAddressList(
        @Query("service") service: String,
        @Query("request") request: String,
        @Query("data") data: String,
        @Query("key") key: String,
        @Query("format") format: String,
        @Query("geometry") geometry: String,
        @Query("attrFilter") attrFilter: String
    ): Call<Welcome7>
}

//data class AddressItem( // 검색 결과에서 하나의 주소 정보를 저장하기 위한 클래스
//    @SerializedName("full_nm")
//    val fullNm: String
//)
//data class AddressList( // 검색 결과(JSON 응답)를 저장하기 위한 클래스 (전체 검색 결과를 저장)
//    @SerializedName("emd_kor_nm")
//    val emdKorNm: String,
//    val results: List<AddressItem>
//)
/*
class AddressSearchHelper {
    //private val api = "http://api.vworld.kr/req/data?service=data&request=GetFeature&data=LT_C_ADEMD_INFO&key=D3D9E0D0-062C-35F0-A49D-FC9E863B3AD5&format=json&geometry=false&attrFilter=emd_kor_nm:like:{읍/면/동}"
    //private val apiKey = "YOUR_API_KEY"

    data class AddressItem( // 검색 결과에서 하나의 주소 정보를 저장하기 위한 클래스
        val fulNm: String
    )
    data class AddressList( // 검색 결과(JSON 응답)를 저장하기 위한 클래스 (전체 검색 결과를 저장)
        val emdKorNm: String,
        val results: List<AddressItem>
    )

    // 검색어로 주소 목록을 가져오는 함수
    fun getAddressList(query: String): List<AddressItem> {
        //val url = "$api?confmKey=$apiKey&currentPage=1&countPerPage=10&keyword=$query&resultType=json"
        val baseApiUrl = "http://api.vworld.kr/req/data"
        val service = "data"
        val requests = "GetFeature"
        val data = "LT_C_ADEMD_INFO"
        val key = "D3D9E0D0-062C-35F0-A49D-FC9E863B3AD5"
        val format = "json"
        val geometry = "false"
        val query = ""

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "$baseApiUrl?service=$service&request=$requests&data=$data&key=$key&format=$format&geometry=$geometry&attrFilter=emd_kor_nm:like:$encodedQuery"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        val body = response.body()?.string()

        // Klaxon을 사용하여 JSON 문자열을 AddressList 객체로 변환
        val addressList = Klaxon().parse<AddressList>(body ?: "")
        return addressList?.results ?: emptyList()
    }
}

 */

class AddressFragment: Fragment(R.layout.address_fragment){
    private lateinit var binding: AddressFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 프래그먼트에서 사용할 레이아웃 파일을 inflate 합니다.
        binding = AddressFragmentBinding.inflate(inflater, container, false)

//        val httpClient = OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor().apply {
//                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
//                else HttpLoggingInterceptor.Level.NONE
//            })
//            .build()


        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("http://api.vworld.kr/")
//            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val addressService = retrofit.create(AddressApiService::class.java)
        val addressList = listOf<Feature>()
        val addressAdapter = AddressAdapter(addressList)
        binding.addressRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색 버튼을 눌렀을 때 처리할 로직 작성
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어가 변경될 때마다 처리할 로직 작성
                //var query = binding.searchView.query.toString()
                addressService.getAddressList(
                    service = "data",
                    request = "GetFeature",
                    data = "LT_C_ADEMD_INFO",
                    key = "D3D9E0D0-062C-35F0-A49D-FC9E863B3AD5",
                    format = "json",
                    geometry = "false",
                    attrFilter = "emd_kor_nm:like:${newText}"
                    //attrFilter = "emd_kor_nm:like:${URLEncoder.encode(query, "UTF-8")}"
                ).enqueue(object : Callback<Welcome7> {
                    override fun onResponse(call: Call<Welcome7>, response: Response<Welcome7>) {

                        //Log.e("Login", "address: ${URLEncoder.encode(query, "UTF-8")}") // 내가 보낸 data Log 출력
                        Log.e("Address", "address: $newText")

                        if (response.isSuccessful) {
                            val code = response.code() // 서버 응답 코드
                            Log.e("AddressApi response code", "is : $code")
                            Log.e("login", "is : ${response.body()?.response?.result?.featureCollection?.features}") // 서버에서 받아온 응답 데이터 log 출력
                            var addressList = response.body()?.response?.result?.featureCollection?.features ?: emptyList()
                            addressAdapter.submitList(addressList)
                        }
                    }

                    override fun onFailure(call: Call<Welcome7>, t: Throwable) {
                        ErrorDialogFragment().show(childFragmentManager, "Address_ErrorDialogFragment")
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
        // onViewCreated() 메서드에서 view를 사용하여 뷰에 대한 작업을 수행합니다.

    }
}
