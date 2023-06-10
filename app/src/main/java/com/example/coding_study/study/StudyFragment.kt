package com.example.coding_study.study

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.common.AddressViewModel
import com.example.coding_study.R
import com.example.coding_study.databinding.StudyFragmentBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Suppress("DEPRECATION")
class StudyFragment : Fragment(R.layout.study_fragment) {
    private lateinit var studyAdapter: StudyAdapter
    private lateinit var binding:StudyFragmentBinding
    private lateinit var viewModel: AddressViewModel
    private lateinit var fab: FloatingActionButton

    // savePostIds 함수 (로컬 저장소에 게시글 번호 저장하는 함수)
    fun savePostIds(context: Context, postIds: List<Long>) {
        val sharedPreferencesPostId = context.getSharedPreferences("MyPostIds", Context.MODE_PRIVATE)
        val editor = sharedPreferencesPostId.edit()
        postIds.forEachIndexed { index, id ->
            editor.putLong("post_$index", id)
        }
        if (!editor.commit()) {
            Log.e("savePostIds", "Failed to save post IDs")
        }
    }

    private fun updateRecyclerViewData(data: List<Post>) {
        if (data.isEmpty()) {
            binding.studyRecyclerView.visibility = View.GONE // RecyclerView를 숨깁니다.
            binding.emptyView.visibility = View.VISIBLE // 빈 리스트를 표시하는 뷰를 보여줍니다.
        } else {
            binding.studyRecyclerView.visibility = View.VISIBLE // RecyclerView를 보여줍니다.
            binding.emptyView.visibility = View.GONE // 빈 리스트를 표시하는 뷰를 숨깁니다.
        }
        studyAdapter.setData(data) // 어댑터의 데이터를 업데이트합니다.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.studyToolBar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true) // 옵션 메뉴 사용을 알림
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // 게시글 목록 뷰 생성
        val view = inflater.inflate(R.layout.study_fragment, container, false)
        binding = StudyFragmentBinding.bind(view)
        val postRecyclerView = binding.studyRecyclerView

        fab = binding.floatingActionButton

        // SwipeRefreshLayout 초기화
        binding.swipeRefreshLayout.setOnRefreshListener { // 게시판을 swipe해서 새로고침하면 새로 추가된 게시글 업로드
            loadStudyList()
        }

        val sharedPreferences = requireActivity().getSharedPreferences("MyAddress", Context.MODE_PRIVATE)
        val myAddress = sharedPreferences?.getString("address", "") // 저장해둔 회원의 주소 가져오기

        var studyAddressTextView = binding.toolbarAddressTextView
        studyAddressTextView.text = "▼$myAddress"

        binding.toolbarAddressTextView.setOnClickListener { // testViewAddress1을 클릭하면 주소 검색 창으로 이동
            val studyAddressFragment = StudyAddressFragment()
            childFragmentManager.beginTransaction()
                .add(R.id.study_fragment_layout, studyAddressFragment, "STUDY_FRAGMENT")
                .addToBackStack("STUDY_FRAGMENT")
                .commit()
        }

        val sharedPreferencesFields = requireActivity().getSharedPreferences("MyFields", Context.MODE_PRIVATE)
        val fieldsString = sharedPreferencesFields?.getString("fields", "")
        val fields = fieldsString?.split(",") ?: emptyList()

        var field1 = binding.myField1
        var field2 = binding.myField2
        field1.text = fields[0]
        field2.text = fields[1]

        val fieldColorsMap = mapOf(
            "안드로이드" to R.color.android,
            "IOS" to R.color.IOS,
            "알고리즘" to R.color.algorithm,
            "데이터베이스" to R.color.database,
            "운영체제" to R.color.os,
            "서버" to R.color.server,
            "웹" to R.color.web,
            "머신러닝" to R.color.machine_learning,
            "기타" to R.color.other
        )

        val fieldBackgroundColorsMap = mapOf(
            "안드로이드" to R.drawable.android_background,
            "IOS" to R.drawable.ios_background,
            "알고리즘" to R.drawable.algorithm_background,
            "데이터베이스" to R.drawable.database_background,
            "운영체제" to R.drawable.os_background,
            "서버" to R.drawable.server_background,
            "웹" to R.drawable.web_background,
            "머신러닝" to R.drawable.machine_learning_background,
            "기타" to R.drawable.other_background
        )


        for (i in 0 until fields.size) {
            val fieldTextView = when (i) {
                0 -> field1
                1 -> field2
                else -> null
            }
            val field = fields.getOrNull(i)
            if (fieldTextView != null && field != null) {
                fieldTextView.text = field
                fieldTextView.setTextColor(ContextCompat.getColor(requireContext(), fieldColorsMap[field] ?: R.color.other))
            }

            val fieldBackgroundDrawableRes = fieldBackgroundColorsMap[field]
            if (fieldBackgroundDrawableRes != null) {
                val fieldBackgroundDrawable = ContextCompat.getDrawable(requireContext(), fieldBackgroundDrawableRes)
                if (fieldTextView != null) {
                    fieldTextView.background = fieldBackgroundDrawable
                }
            }
        }

        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity()).get(AddressViewModel::class.java)

        // 가져온 데이터를(AddressFragment에서 선택한 주소) 사용해서 textViewAddress1 업데이트
        viewModel.getSelectedAddress().observe(viewLifecycleOwner) { address ->
            if (address == "") { // 주소 검색란에 검색어가 없을 경우
                binding.toolbarAddressTextView.text = "▼$myAddress" // 원래 저장된 주소 띄우기
            } else {
                binding.toolbarAddressTextView.text = "▼$address"
            }

            loadStudyList()

            Log.e("StudyFragment", "Change address: $address") // 선택된 address 변수 값 로그 출력
        }

        // 게시글을 클릭할 때 서버에 토큰, 게시글 id를 주고 Role과 게시글 정보를 받아옴. 이후 Role에 따라 다른 레이아웃 띄우기
        var onItemClickListener: StudyAdapter.OnItemClickListener = object :
            StudyAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) { // 게시글 클릭 시
                // 저장된 게시글 id 가져오기
                val sharedPreferencesPostId = requireActivity().getSharedPreferences("MyPostIds", Context.MODE_PRIVATE) // "MyPostIds" 라는 이름으로 SharedPreferences 객체를 생성
                val size = sharedPreferencesPostId.all.size // SharedPreferences 객체에 저장된 모든 키-값 쌍의 개수를 구함
                val postIds = (0 until size).mapNotNull { // 0부터 size-1까지의 정수를 순회하면서, 해당하는 키("post_0", "post_1", ...)에 대한 값을 리스트에 추가, 함수를 적용한 결과 중 null이 아닌 값들로만 리스트를 만듬
                    val postId = sharedPreferencesPostId.getLong("post_$it", -1) // "post_$it"라는 이름으로 저장된 Long 타입의 값 가져오기, 해당 키가 존재하지 않으면 -1 반환
                    if (postId != -1L) postId else null// postId가 -1L 이 아닐 경우 해당 값을 유지하고, -1L일 경우 null 반환
                }
                Log.e("StudyFragment","postIds: $postIds")

                val selectedPostId = postIds.getOrNull(position)// postIds 리스트에서 position에 해당하는 인덱스의 값을 가져옴
                Log.e("StudyFragment","selectedPostId: $selectedPostId")

                //저장된 토큰값 가져오기
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

                val studyOnlyService = retrofitBearer.create(StudyOnlyService::class.java)

                if (selectedPostId != null) {
                    studyOnlyService.getOnlyPost(selectedPostId).enqueue(object : Callback<StudyOnlyResponse>{
                        override fun onResponse(call: Call<StudyOnlyResponse>, response: Response<StudyOnlyResponse>) {

                            if (response.isSuccessful){
                                val studyOnlyResponse = response.body() // 서버에서 받아온 응답 데이터
                                val code = response.code() // 서버 응답 코드
                                Log.e("StudyOnlyResponse_response.body", "is : ${response.body()}") // 서버에서 받아온 응답 데이터 log 출력
                                Log.e("StudyOnlyResponse_response.code", "is : $code") // 서버 응답 코드 log 출력

                                if (studyOnlyResponse?.result == true && studyOnlyResponse.data.containsKey(
                                        Role.HOST
                                    )){ // Role이 호스트인 경우
                                    // StudyHostFragment로 게시글 정보를 넘겨주기 위해 받은 데이터 저장
                                    val recruitment = studyOnlyResponse.data[Role.HOST] as Any
                                    val gson = Gson()
                                    val json = gson.toJson(recruitment)
                                    val bundle = Bundle()
                                    bundle.putString("recruitmentJson", json)
                                    val hostFragment = StudyHostFragment()
                                    hostFragment.arguments = bundle

                                    val slideAnimation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f, 0, 0f, 0, 0f)
                                    slideAnimation.duration = 200
                                    view.startAnimation(slideAnimation)

                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.study_fragment_layout, hostFragment)
                                        .addToBackStack(null)
                                        .commit()

                                } else if (studyOnlyResponse?.result == true && studyOnlyResponse.data.containsKey(
                                        Role.GUEST
                                    )){ // Role이 게스트인 경우
                                    // StudyGuestFragment로 게시글 정보를 넘겨주기 위해 받은 데이터 저장
                                    val recruitment = studyOnlyResponse.data[Role.GUEST] as Any
                                    val participantExist = studyOnlyResponse.data[Role.PARTICIPATION] as Boolean // participantExist이 true면 취소하기 버튼, false면 참여하기 버튼
                                    val gson = Gson()
                                    val json = gson.toJson(recruitment)
                                    val bundle = Bundle()

                                    bundle.putString("recruitmentJson", json)
                                    bundle.putBoolean("participateJson", participantExist)

                                    val guestFragment = StudyGuestFragment()
                                    guestFragment.arguments = bundle

                                    val slideAnimation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f, 0, 0f, 0, 0f)
                                    slideAnimation.duration = 200
                                    view.startAnimation(slideAnimation)

                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.study_fragment_layout, guestFragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                            else{
                                Log.e("studyOnlyResponse onResponse","But not success")
                            }
                        }

                        override fun onFailure(call: Call<StudyOnlyResponse>, t: Throwable) {
                            Log.e("StudyFragment_StudyOnlyResponse", "Failed to get study list", t)
                            Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }

        studyAdapter = StudyAdapter(listOf(), onItemClickListener) // 어댑터 초기화 설정
        postRecyclerView.adapter = studyAdapter
        binding.studyRecyclerView.layoutManager = LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정

        binding.floatingActionButton.setOnClickListener { // +버튼 (글쓰기 버튼) 눌렀을 때
            val studyuploadFragment = StudyUpload() // StudyUploadFragment로 변경
            childFragmentManager.beginTransaction()
                .addToBackStack("STUDY_FRAGMENT")
                .replace(R.id.study_fragment_layout, studyuploadFragment, "STUDY_FRAGMENT")
                .commit()
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        Log.e("onPause", "***********")
    }

    // onResume에서 loadStudyList() 함수 호출
    override fun onResume() {
        super.onResume()
        Log.e("StudyFragment", "onResume---------------------------")
        loadStudyList()
    }

    fun showFloatingButton() {
        fab.show()
        binding.myField1.visibility = View.VISIBLE
        binding.myField2.visibility = View.VISIBLE
    }
    fun hideFloatingButton() {
        fab.hide()
        binding.myField1.visibility = View.GONE
        binding.myField2.visibility = View.GONE

    }

    private fun loadStudyList() { // 서버에서 게시글 전체를 가져와서 로드하는 함수
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

        val studyService = retrofitBearer.create(StudyGetService::class.java)
        val binding = view?.let { StudyFragmentBinding.bind(it) }
        val changeAddress = viewModel.getSelectedAddress().value

        //스터디 게시글 가져오기
        studyService.studyGetList(address = changeAddress).enqueue(object : Callback<StudyListResponse> {
            override fun onResponse(call: Call<StudyListResponse>, response: Response<StudyListResponse>
            ) {
                if (changeAddress != null) {
                    Log.e("changeAddress", changeAddress)
                }
                if (response.isSuccessful) {
                    val studyListResponse = response.body() // 서버에서 받아온 응답 데이터
                    val code = response.code() // 서버 응답 코드
                    Log.e("StudyList_response.body", "is : $studyListResponse") // 서버에서 받아온 응답 데이터 log 출력
                    Log.e("response code", "is : $code") // 서버 응답 코드 log 출력

                    val studyList = studyListResponse?.data
                    val postListResponse = studyList?.map {
                        Post( it.nickname, it.title, it.content, it.currentCount, it.count, it.field, it.currentDateTime, it.profileImagePath)
                    } ?: emptyList()
                    //studyList의 형식은 List<RecruitmentDto>이므로 서버에서 받은 게시글을 postList에 넣어주기 위해 List<Post>로 변환

                    if (studyListResponse?.result == true) {

                        if (studyList != null) {
                            if (studyList.isEmpty()) {
                                // 데이터가 없을 경우
                                Log.e("StudyFragment studyList is empty", studyList.toString())
                                updateRecyclerViewData(emptyList()) // 빈 리스트를 전달하여 데이터를 업데이트합니다.
                            } else {
                                updateRecyclerViewData(postListResponse) // 데이터가 있는 경우
                                val recruitmentIds =
                                    studyListResponse.data?.map { it.recruitmentId } // 게시물 아이디 리스트 추출

                                if (recruitmentIds != null) {
                                    context?.let { savePostIds(it, recruitmentIds) } // 게시물 아이디 리스트 저장
                                }
                                Log.e("StudyFragment", "recruitmentIds: $recruitmentIds")

                                studyAdapter.postList = postListResponse //.reversed() // 어댑터의 postList 변수 업데이트 (reversed()를 이용해서 리스트를 역순으로 정렬하여 최신글이 가장 위에 뜨게 됨)
                                studyAdapter.notifyDataSetChanged() // notifyDataSetChanged() 메서드를 호출하여 변경 내용을 화면에 반영

                                if (binding != null) {
                                    binding.swipeRefreshLayout.isRefreshing = false // 새로고침 상태를 false로 변경해서 새로고침 완료
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<StudyListResponse>, t: Throwable) {
                Log.e("StudyFragment", "Failed to get study list", t)
                Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu_study, menu)

        viewModel = ViewModelProvider(requireActivity()).get(AddressViewModel::class.java)
        val searchPostAddress = viewModel.getSelectedAddress().value

        val newSearch = binding.toolbarStudySearch
        val retrofit = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val studySearchService = retrofit.create(StudySearchService::class.java)

        newSearch.setOnQueryTextListener(object :SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { // 검색 버튼을 누를 때 호출
                studySearchService.studySearch(keyword = "$query", address = searchPostAddress).enqueue(object :Callback<StudyListResponse>{
                    override fun onResponse(call: Call<StudyListResponse>, response: Response<StudyListResponse>
                    ) {
                        Log.e("StudySearch", "keyword: $query, address= $searchPostAddress")
                        Log.e("studySearchService", "onResponse")
                        if (response.isSuccessful) {
                            val studyListResponse = response.body() // 서버에서 받아온 응답 데이터
                            Log.e("StudySearchService response code is","${response.code()}")
                            Log.e("StudySearchService response body is", "$studyListResponse")

                            val studyList = studyListResponse?.data
                            val postListResponse = studyList?.map {
                                Post( it.nickname, it.title, it.content, it.currentCount, it.count, it.field, it.currentDateTime, it.profileImagePath)
                            } ?: emptyList()
                            //studyList의 형식은 List<RecruitmentDto>이므로 서버에서 받은 게시글을 postList에 넣어주기 위해 List<Post>로 변환

                            val searchListId = studyList?.map { it.recruitmentId }
                            if (searchListId != null) {
                                context?.let { savePostIds(it, searchListId) }
                            }

                            if (studyListResponse?.result == true) {
                                studyAdapter.postList = postListResponse // 어댑터의 postList 변수 업데이트
                                studyAdapter.notifyDataSetChanged() // notifyDataSetChanged() 메서드를 호출하여 변경 내용을 화면에 반영

                            }
                        }
                        else {
                            Log.e("StudySearchService onResponse", "But not success")
                        }
                    }

                    override fun onFailure(call: Call<StudyListResponse>, t: Throwable) {
                        Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_LONG).show()
                    }
                })
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean { // 검색어가 바뀔때마다 호출

                return false
            }
        })
    }
}
