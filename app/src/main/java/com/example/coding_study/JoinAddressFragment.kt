package com.example.coding_study

import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.coding_study.databinding.JoinAddressBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/*
interface ApiService{
    @POST("members/signUp")
    fun searchAddress(@Body searchaddress: AddressRequest): Call<AddressResponse>
}

//요청값
data class AddressRequest (
    val address: String =""
)

//응답값
data class AddressResponse(
    val address: String
)
 */

/*
class JoinAddressFragment : Fragment(R.layout.join_address), SearchView.OnQueryTextListener {

    private lateinit var binding: JoinAddressBinding
    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 뷰 바인딩을 초기화합니다.
        binding = JoinAddressBinding.bind(view)

        // 뷰 바인딩에서 SearchView를 가져옵니다.
        searchView = binding.searchView

        // SearchView에 이벤트 리스너를 추가합니다.
        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        // 검색 버튼을 눌렀을 때 호출됩니다.
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // 검색어가 변경될 때마다 호출됩니다.
        return false
    }
}

 */


class JoinAddressFragment : Fragment(R.layout.join_address) {

    private var _binding: JoinAddressBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = JoinAddressBinding.inflate(inflater, container, false)
        val view = binding.root

        // SearchView를 초기화합니다.
        searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색 버튼을 눌렀을 때 호출됩니다.
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어가 변경될 때마다 호출됩니다.
                return false
            }
        })

        // View를 반환합니다.
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}