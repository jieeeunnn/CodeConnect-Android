package com.example.coding_study
/*
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.example.coding_study.databinding.ActivityJoinAddressBinding
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URLEncoder
import kotlin.collections.forEach
/*
interface AddressService {
    @GET("search/address")
    fun searchAddress(
        //@Query("full_nm") fullNm: String?,
        @Query("emd_kor_nm") emdKorNm: String?
    ): Call<List<Address>>
}

data class Address(
    @SerializedName("full_nm")
    val fullNm: String,
    @SerializedName("emd_kor_nm")
    val emdKorNm: String
)


 */

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
        val query = "서울특별시 강남구 역삼동"

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


class AddressSearchHelper {
    private val apiService: AddressApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://api.vworld.kr/req/data/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
            .create(AddressApiService::class.java)
    }

    data class AddressItem(
        val fulNm: String
    )

    data class AddressList(
        val emdKorNm: String,
        val results: List<AddressItem>
    )

    fun getAddressList(query: String): List<AddressItem> {
        val service = "data"
        val requests = "GetFeature"
        val data = "LT_C_ADEMD_INFO"
        val key = "D3D9E0D0-062C-35F0-A49D-FC9E863B3AD5"
        val format = "json"
        val geometry = "false"

        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val response = apiService.getAddressList(
            service = service,
            request = requests,
            data = data,
            key = key,
            format = format,
            geometry = geometry,
            attrFilter = "emd_kor_nm:like:$encodedQuery"
        )

        // 주소 목록을 가져와서 List<AddressItem>으로 변환
        val addressList = response.body()
        return addressList?.results ?: emptyList()
    }
}

interface AddressApiService {
    @GET("http://api.vworld.kr/req/data")
    suspend fun getAddressList(
        @Query("service") service: String,
        @Query("request") request: String,
        @Query("data") data: String,
        @Query("key") key: String,
        @Query("format") format: String,
        @Query("geometry") geometry: String,
        @Query("attrFilter") attrFilter: String
    ): Call<AddressSearchHelper.AddressList>
}

class JoinAddressActivity : AppCompatActivity() {
    private lateinit var searchView: SearchView
    private lateinit var addressRecyclerView: RecyclerView
    private lateinit var addressListAdapter: AddressListAdapter

    private val addressSearchHelper = AddressSearchHelper()

    private lateinit var binding: ActivityJoinAddressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchView = findViewById(R.id.searchView)
        addressRecyclerView = findViewById(R.id.recyclerView)

        // RecyclerView 초기화
        addressListAdapter = AddressListAdapter(emptyList()) { address ->
            // 주소 선택 시 처리할 작업
            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show()
        }
        addressRecyclerView.adapter = addressListAdapter
        addressRecyclerView.layoutManager = LinearLayoutManager(this)

        // SearchView 초기화
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean { // 검색어가 변경될 때마다 호출
                if (newText.isNullOrEmpty()) { // 검색어가 없는 경우
                    addressListAdapter = AddressListAdapter(emptyList()) { } // 빈 리스트를 어댑터에 전달하여 리사이클러뷰를 초기화
                    addressRecyclerView.adapter = addressListAdapter
                } else { // 검색어가 있는 경우
                    val addressList = addressSearchHelper.getAddressList(newText) // 검색어로 주소 목록을 가져옴
                    addressListAdapter = AddressListAdapter(addressList) { address -> // 이 주소 목록은 AddressListAdapter의 생성자로 전달되며, AddressListAdapter는 주소 목록을 리스트 아이템으로 변환하여 리사이클러뷰에 표시
                        // 주소 선택 시 처리할 작업
                        Toast.makeText(this@JoinAddressActivity, address.toString(), Toast.LENGTH_SHORT).show()
                    }
                    addressRecyclerView.adapter = addressListAdapter
                }
                return true
            }
        })
    }


        /*
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val addressService = retrofit.create(AddressService::class.java)



        //searchView 설정
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { // 검색버튼 입력시 호출, 검색 버튼이 없으면 사용 X
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean { // 텍스트 입력/수정 시에 호출
                newText?.let {
                    if (it.length > 0) {
                        // fullNm(ex. 서울특별시 성북구 성북동), emdKorNm(ex. 성북동) 값을 추출
                        val values = it.split(" ")
                        //val fullNm = values[0]
                        val emdKorNm = values[1]
                        // Retrofit을 사용하여 API 호출
                        addressService.searchAddress(emdKorNm).enqueue(object : Callback<List<Address>> {
                            override fun onResponse(call: Call<List<Address>>, response: Response<List<Address>>) {
                                if (response.isSuccessful) {
                                    val addressList = mutableListOf<Address>()
                                    response.body()?.forEach {
                                        val address = Address(it.fullNm, it.emdKorNm)
                                        addressList.add(address)
                                    }
                                    //val addressList = response.body()
                                    Log.e("JoinAddress", "is : ${response.body()}")
                                    // RecyclerView에 주소 목록 표시
                                }
                            }

                            override fun onFailure(call: Call<List<Address>>, t: Throwable) {
                                // 오류 처리
                            }
                        })
                    }
                }
                return true
            }
        })

         */
    }
//}

 */