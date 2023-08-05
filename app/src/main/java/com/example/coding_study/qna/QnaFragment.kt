package com.example.coding_study.qna

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.R
import com.example.coding_study.databinding.QnaFragmentBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("DEPRECATION")
class QnAFragment : Fragment(R.layout.qna_fragment) {
    private lateinit var binding:QnaFragmentBinding
    private lateinit var qnaAdapter: QnaAdapter
    private lateinit var topQnaAdapter: QnaAdapter
    private lateinit var qnaFab: FloatingActionButton

    // saveQnaPostIds 함수 (로컬 저장소에 Qna 게시글Id 저장하는 함수)
    fun saveQnaPostIds(context: Context, qnaPostIds: List<Long>) {
        val sharedPreferencesQnaPostId = context.getSharedPreferences("QnaPostIds", Context.MODE_PRIVATE)
        val editor = sharedPreferencesQnaPostId.edit()
        qnaPostIds.forEachIndexed { index, id ->
            editor.putLong("qnaPost_$index", id)
        }
        if (!editor.commit()) {
            Log.e("saveQnaPostIds", "Failed to save QnaPost IDs")
        }
    }

    // saveQnaPostIds 함수 (로컬 저장소에 Qna 게시글Id 저장하는 함수)
    fun saveTopQnaPostIds(context: Context, qnaPostIds: List<Long>) {
        val sharedPreferencesQnaPostId = context.getSharedPreferences("TopQnaPostIds", Context.MODE_PRIVATE)
        val editor = sharedPreferencesQnaPostId.edit()
        qnaPostIds.forEachIndexed { index, id ->
            editor.putLong("topQnaPost_$index", id)
        }
        if (!editor.commit()) {
            Log.e("saveTopQnaPostIds", "Failed to save TopQnaPost IDs")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.qnaToolBar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true) // 옵션 메뉴 사용을 알림

        toolbar.title = ""
        toolbar.subtitle = ""
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.qna_fragment, container, false)
        binding = QnaFragmentBinding.bind(view)
        val qnaPostRecyclerView = binding.qnaRecyclerView
        val topQnaRecyclerView = binding.qnaTopRecyclerView
        qnaFab = binding.qnaFloatingActionButton

        // SwipeRefreshLayout 초기화
        binding.qnaSwifeRefreshLayout.setOnRefreshListener {
            loadQnaList()
            loadTopQnaList()
        }


        var onQnaClickListener: QnaAdapter.OnQnaClickListener = object :
            QnaAdapter.OnQnaClickListener { // 전체 게시글 클릭시
            override fun onQnaClick(position: Int) {
                Log.e("QnaFragment", "onQnaClick!!!")

                // 저장된 Qna 게시글 id 가져오기
                val sharedPreferencesQnaPostId = requireActivity().getSharedPreferences("QnaPostIds", Context.MODE_PRIVATE) // "MyPostIds" 라는 이름으로 SharedPreferences 객체를 생성
                val size = sharedPreferencesQnaPostId.all.size // SharedPreferences 객체에 저장된 모든 키-값 쌍의 개수를 구함
                val qnaPostIds = (0 until size).mapNotNull { // 0부터 size-1까지의 정수를 순회하면서, 해당하는 키("post_0", "post_1", ...)에 대한 값을 리스트에 추가, 함수를 적용한 결과 중 null이 아닌 값들로만 리스트를 만듬
                    val postId = sharedPreferencesQnaPostId.getLong("qnaPost_$it", -1) // "post_$it"라는 이름으로 저장된 Long 타입의 값 가져오기, 해당 키가 존재하지 않으면 -1 반환
                    if (postId != -1L) postId else null// postId가 -1L 이 아닐 경우 해당 값을 유지하고, -1L일 경우 null 반환
                }
                Log.e("QnaFragment","QnaPostIds: $qnaPostIds")

                val qnaSelectedPostId = qnaPostIds.getOrNull(position)// qnaPostIds 리스트에서 position에 해당하는 인덱스의 값을 가져옴
                Log.e("QnaFragment","qnaSelectedPostId: $qnaSelectedPostId")

                //저장된 토큰값 가져오기
                val sharedPreferences = requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
                val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

                val retrofitBearer = Retrofit.Builder()
                    .baseUrl("http://52.79.53.62:8080/")
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

                val qnaOnlyService = retrofitBearer.create(QnaOnlyService::class.java)

                if(qnaSelectedPostId != null) {
                    qnaOnlyService.qnaGetOnlyPost(qnaSelectedPostId).enqueue(object :Callback<QnaOnlyResponse>{
                        override fun onResponse(call: Call<QnaOnlyResponse>, response: Response<QnaOnlyResponse>
                        ) {
                            if (response.isSuccessful) {
                                val qnaOnlyResponse = response.body() // 서버에서 받아온 응답 데이터
                                Log.e("QnaOnlyResponse_reponse.body", "is : $qnaOnlyResponse")
                                Log.e("QnaOnlyResponse_response.code", "is : ${response.code()}")

                                if (qnaOnlyResponse?.result == true && qnaOnlyResponse.data.containsKey(
                                        QnaRole.HOST
                                    )) {
                                    // QnaHostFragment로 게시글 정보를 넘겨주기 위해 받은 데이터 저장
                                    val qnaRecruitment = qnaOnlyResponse.data[QnaRole.HOST] as Any
                                    val commentHost = qnaOnlyResponse.data[QnaRole.COMMENT_HOST] as? List<Comment>
                                    val commentGuest = qnaOnlyResponse.data[QnaRole.COMMENT_GUEST] as? List<Comment>
                                    Log.e("QnaFragment onlyResponse Bundle commentHost", "$commentHost")
                                    Log.e("QnaFragment onlyResponse Bundle commentGuest", "$commentGuest")

                                    val qnaGson = Gson()
                                    val qnaBundle = Bundle()

                                    val qnaJson = qnaGson.toJson(qnaRecruitment)
                                    val commentHostJson = qnaGson.toJson(commentHost)
                                    val commentGuestJson = qnaGson.toJson(commentGuest)

                                    qnaBundle.putString("qnaHostRecruitmentJson", qnaJson)
                                    qnaBundle.putString("commentHostJson", commentHostJson)
                                    qnaBundle.putString("commentGuestJson", commentGuestJson)

                                    val qnaHostFragment = QnaHostFragment()
                                    qnaHostFragment.arguments = qnaBundle

                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.qna_fragment_layout, qnaHostFragment)
                                        .addToBackStack(null)
                                        .commit()

                                } else if (qnaOnlyResponse?.result == true && qnaOnlyResponse.data.containsKey(
                                        QnaRole.GUEST
                                    )) {
                                    val qnaRecruitment = qnaOnlyResponse.data[QnaRole.GUEST] as Any
                                    val commentHost = qnaOnlyResponse.data[QnaRole.COMMENT_HOST] as? List<Comment>
                                    val commentGuest = qnaOnlyResponse.data[QnaRole.COMMENT_GUEST] as? List<Comment>
                                    Log.e("QnaFragment Guest onlyResponse Bundle commentHost", "$commentHost")
                                    Log.e("QnaFragment Guest onlyResponse Bundle commentGuest", "$commentGuest")


                                    val qnaGson = Gson()
                                    val qnaBundle = Bundle()

                                    val qnaJson = qnaGson.toJson(qnaRecruitment)
                                    val commentHostJson = qnaGson.toJson(commentHost)
                                    val commentGuestJson = qnaGson.toJson(commentGuest)

                                    qnaBundle.putString("qnaGuestRecruitmentJson", qnaJson)
                                    qnaBundle.putString("commentHostJson", commentHostJson)
                                    qnaBundle.putString("commentGuestJson", commentGuestJson)

                                    val qnaGuestFragment = QnaGuestFragment()
                                    qnaGuestFragment.arguments = qnaBundle

                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.qna_fragment_layout, qnaGuestFragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                            else {
                                Log.e("qnaOnlyResponse onResponse", "But not success")
                            }
                        }

                        override fun onFailure(call: Call<QnaOnlyResponse>, t: Throwable) {
                            Log.e("QnaFragment_QnaOnlyResponse", "Failed to get qna list", t)
                        }

                    })
                }

            }
        }

        var onPopularClickListener: QnaAdapter.OnPopularClickListener = object :
            QnaAdapter.OnPopularClickListener { // 인기 게시글 클릭 시
            override fun onPopularClick(position: Int) {
                Log.e("QnaFragment", "onQnaClick!!!")

                // 저장된 Qna 게시글 id 가져오기
                val sharedPreferencesTopQnaPostId = requireActivity().getSharedPreferences("TopQnaPostIds", Context.MODE_PRIVATE) // "MyPostIds" 라는 이름으로 SharedPreferences 객체를 생성
                val size = sharedPreferencesTopQnaPostId.all.size // SharedPreferences 객체에 저장된 모든 키-값 쌍의 개수를 구함
                val topQnaPostIds = (0 until size).mapNotNull { // 0부터 size-1까지의 정수를 순회하면서, 해당하는 키("post_0", "post_1", ...)에 대한 값을 리스트에 추가, 함수를 적용한 결과 중 null이 아닌 값들로만 리스트를 만듬
                    val postId = sharedPreferencesTopQnaPostId.getLong("topQnaPost_$it", -1) // "post_$it"라는 이름으로 저장된 Long 타입의 값 가져오기, 해당 키가 존재하지 않으면 -1 반환
                    if (postId != -1L) postId else null// postId가 -1L 이 아닐 경우 해당 값을 유지하고, -1L일 경우 null 반환
                }
                Log.e("QnaFragment","QnaPostIds: $topQnaPostIds")

                val qnaSelectedPostId = topQnaPostIds.getOrNull(position)// qnaPostIds 리스트에서 position에 해당하는 인덱스의 값을 가져옴
                Log.e("QnaFragment","qnaSelectedPostId: $qnaSelectedPostId")

                //저장된 토큰값 가져오기
                val sharedPreferences = requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
                val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

                val retrofitBearer = Retrofit.Builder()
                    .baseUrl("http://52.79.53.62:8080/")
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

                val qnaOnlyService = retrofitBearer.create(QnaOnlyService::class.java)

                if(qnaSelectedPostId != null) {
                    qnaOnlyService.qnaGetOnlyPost(qnaSelectedPostId).enqueue(object :Callback<QnaOnlyResponse>{
                        override fun onResponse(call: Call<QnaOnlyResponse>, response: Response<QnaOnlyResponse>
                        ) {
                            if (response.isSuccessful) {
                                val qnaOnlyResponse = response.body() // 서버에서 받아온 응답 데이터
                                Log.e("QnaOnlyResponse_reponse.body", "is : $qnaOnlyResponse")
                                Log.e("QnaOnlyResponse_response.code", "is : ${response.code()}")

                                if (qnaOnlyResponse?.result == true && qnaOnlyResponse.data.containsKey(
                                        QnaRole.HOST
                                    )) {
                                    // QnaHostFragment로 게시글 정보를 넘겨주기 위해 받은 데이터 저장
                                    val qnaRecruitment = qnaOnlyResponse.data[QnaRole.HOST] as Any
                                    val commentHost = qnaOnlyResponse.data[QnaRole.COMMENT_HOST] as? List<Comment>
                                    val commentGuest = qnaOnlyResponse.data[QnaRole.COMMENT_GUEST] as? List<Comment>
                                    Log.e("QnaFragment onlyResponse Bundle commentHost", "$commentHost")
                                    Log.e("QnaFragment onlyResponse Bundle commentGuest", "$commentGuest")

                                    val qnaGson = Gson()
                                    val qnaBundle = Bundle()

                                    val qnaJson = qnaGson.toJson(qnaRecruitment)
                                    val commentHostJson = qnaGson.toJson(commentHost)
                                    val commentGuestJson = qnaGson.toJson(commentGuest)

                                    qnaBundle.putString("qnaHostRecruitmentJson", qnaJson)
                                    qnaBundle.putString("commentHostJson", commentHostJson)
                                    qnaBundle.putString("commentGuestJson", commentGuestJson)

                                    val qnaHostFragment = QnaHostFragment()
                                    qnaHostFragment.arguments = qnaBundle

                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.qna_fragment_layout, qnaHostFragment)
                                        .addToBackStack(null)
                                        .commit()

                                } else if (qnaOnlyResponse?.result == true && qnaOnlyResponse.data.containsKey(
                                        QnaRole.GUEST
                                    )) {
                                    val qnaRecruitment = qnaOnlyResponse.data[QnaRole.GUEST] as Any
                                    val commentHost = qnaOnlyResponse.data[QnaRole.COMMENT_HOST] as? List<Comment>
                                    val commentGuest = qnaOnlyResponse.data[QnaRole.COMMENT_GUEST] as? List<Comment>
                                    Log.e("QnaFragment Guest onlyResponse Bundle commentHost", "$commentHost")
                                    Log.e("QnaFragment Guest onlyResponse Bundle commentGuest", "$commentGuest")


                                    val qnaGson = Gson()
                                    val qnaBundle = Bundle()

                                    val qnaJson = qnaGson.toJson(qnaRecruitment)
                                    val commentHostJson = qnaGson.toJson(commentHost)
                                    val commentGuestJson = qnaGson.toJson(commentGuest)

                                    qnaBundle.putString("qnaGuestRecruitmentJson", qnaJson)
                                    qnaBundle.putString("commentHostJson", commentHostJson)
                                    qnaBundle.putString("commentGuestJson", commentGuestJson)

                                    val qnaGuestFragment = QnaGuestFragment()
                                    qnaGuestFragment.arguments = qnaBundle

                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.qna_fragment_layout, qnaGuestFragment)
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                            else {
                                Log.e("qnaOnlyResponse onResponse", "But not success")
                            }
                        }

                        override fun onFailure(call: Call<QnaOnlyResponse>, t: Throwable) {
                            Log.e("QnaFragment_QnaOnlyResponse", "Failed to get qna list", t)
                        }

                    })
                }
            }
        }

        qnaAdapter = QnaAdapter(listOf(), onQnaClickListener, onPopularClickListener) // 어댑터 초기화 설정
        qnaPostRecyclerView.adapter = qnaAdapter
        binding.qnaRecyclerView.layoutManager = LinearLayoutManager(context) // 어떤 layout을 사용할 것인지 결정

        topQnaAdapter = QnaAdapter(listOf(), onQnaClickListener, onPopularClickListener) // 인기 게시글용 어댑터 초기화 설정
        topQnaRecyclerView.adapter = topQnaAdapter
        topQnaRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) // 인기 게시글은 가로로 배열

        binding.qnaFloatingActionButton.setOnClickListener { // +버튼 (글쓰기 버튼) 눌렀을 때
            val qnaUploadFragment = QnaUpload() // StudyUploadFragment로 변경
            childFragmentManager.beginTransaction()
                .addToBackStack("QNA_FRAGMENT")
                .replace(R.id.qna_fragment_layout, qnaUploadFragment, "QNA_FRAGMENT")
                .commit()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadQnaList()
        loadTopQnaList()
    }

    fun showFloatingButton() {
        qnaFab.show()
    }

    fun hideFloatingButton() {
        qnaFab.hide()
    }

    private fun loadQnaList() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://52.79.53.62:8080/")
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

        val qnaService = retrofitBearer.create(QnaGetService::class.java)
        val binding = view?.let { QnaFragmentBinding.bind(it)}

        qnaService.qnaGetList().enqueue(object : Callback<QnaListResponse> {
            override fun onResponse(
                call: Call<QnaListResponse>,
                response: Response<QnaListResponse>
            ) {
                if (response.isSuccessful) {
                    val qnaListResponse = response.body()
                    Log.e("QnaList_response.body", "is: $qnaListResponse")
                    Log.e("QnaList_response.code", "is: ${response.code()}")

                    val qnaList = qnaListResponse?.data
                    val qnapostListResponse = qnaList?.map { QnaPost(it.nickname, it.title, it.content, it.currentDateTime, it.commentCount, it.profileImagePath, it.likeCount, false) }
                    //qnaList의 형식은 List<QnaUploadDto>이므로 서버에서 받은 게시글을 qnaPostList에 넣어주기 위해 List<qnaPost>로 변환

                    if (qnaListResponse?.result == true) {
                        val qnaRecruitmentIds = qnaListResponse.data?.map { it.qnaId } // qna 게시물 아이디 리스트 추출

                        if (qnaRecruitmentIds != null) {
                            context?.let { saveQnaPostIds(it, qnaRecruitmentIds) } // 게시물 아이디 리스트 저장
                        }
                        Log.e("StudyFragment", "recruitmentIds: $qnaRecruitmentIds")

                        if (qnapostListResponse != null) {
                            qnaAdapter.qnaPostList = qnapostListResponse
                            qnaAdapter.notifyDataSetChanged()
                        }
                        if (binding != null) {
                            binding.qnaSwifeRefreshLayout.isRefreshing = false // 새로고침 상태를 false로 변경해서 새로고침 완료
                        }
                    }
                } else {
                    Log.e("qnaFragment qna list", "onResponse but not success")
                    Log.e("QnaList_response.code", "is: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<QnaListResponse>, t: Throwable) {
                Log.e("QnaFragment", "Failed to get qna list")
            }

        })
    }


    private fun loadTopQnaList() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyToken", Context.MODE_PRIVATE)
        val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://52.79.53.62:8080/")
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

        val topQnaGetService = retrofitBearer.create(TopQnaGetService::class.java)

        topQnaGetService.topQnaGetList().enqueue(object : Callback<QnaListResponse> {
            override fun onResponse(
                call: Call<QnaListResponse>,
                response: Response<QnaListResponse>
            ) {
                if (response.isSuccessful) {
                    val qnaListResponse = response.body()
                    Log.e("TopQnaList_response.body", "is: $qnaListResponse")
                    Log.e("TopQnaList_response.code", "is: ${response.code()}")

                    val qnaList = qnaListResponse?.data
                    val qnapostListResponse = qnaList?.map { QnaPost(it.nickname, it.title, it.content, it.currentDateTime, it.commentCount, it.profileImagePath, it.likeCount, true) }
                    //qnaList의 형식은 List<QnaUploadDto>이므로 서버에서 받은 게시글을 qnaPostList에 넣어주기 위해 List<qnaPost>로 변환

                    if (qnaListResponse?.result == true) {
                        val qnaRecruitmentIds = qnaListResponse.data?.map { it.qnaId } // qna 게시물 아이디 리스트 추출

                        if (qnaRecruitmentIds != null) {
                            context?.let { saveTopQnaPostIds(it, qnaRecruitmentIds) } // 게시물 아이디 리스트 저장
                        }
                        Log.e("QnaFragment", "recruitmentIds: $qnaRecruitmentIds")

                        if (qnapostListResponse != null) {
                            topQnaAdapter.qnaPostList = qnapostListResponse
                            topQnaAdapter.notifyDataSetChanged()
                        }
                        if (binding != null) {
                            binding.qnaSwifeRefreshLayout.isRefreshing = false // 새로고침 상태를 false로 변경해서 새로고침 완료
                        }
                    }
                } else {
                    Log.e("qnaFragment top qna list", "onResponse but not success")
                    Log.e("QnaList_response.code", "is: ${response.code()}")
                }            }

            override fun onFailure(call: Call<QnaListResponse>, t: Throwable) {
                Log.e("QnaFragment", "Failed to get top qna list")
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_menu_qna, menu)
        Log.e("qnaSearchView", "onCreateOptionsMenu")

        val searchView = binding.toolbarQnaNewSearch
        val retrofit = Retrofit.Builder()
            .baseUrl("http://52.79.53.62:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val qnaSearchService = retrofit.create(QnaSearchService::class.java)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                qnaSearchService.qnaSearch("$query").enqueue(object : Callback<QnaListResponse> {
                    override fun onResponse(
                        call: Call<QnaListResponse>, response: Response<QnaListResponse>
                    ) {
                        Log.e("QnaSearch", "keyword: $query")
                        Log.e("QnaSearchService", "response code : ${response.code()}")

                        if (response.isSuccessful) {
                            val qnaListResponse = response.body()
                            Log.e("QnaSearchService response code is", "${response.code()}")

                            val qnaList = qnaListResponse?.data
                            val qnapostListResponse = qnaList?.map {
                                QnaPost(it.nickname, it.title, it.content, it.currentDateTime, it.commentCount, it.profileImagePath, it.likeCount, false)
                            }

                            val searchQnaId = qnaList?.map { it.qnaId }
                            context?.let {
                                if (searchQnaId != null) {
                                    saveQnaPostIds(it, searchQnaId)
                                }
                            }

                            if (qnapostListResponse != null) {
                                qnaAdapter.qnaPostList = qnapostListResponse
                                qnaAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                    override fun onFailure(call: Call<QnaListResponse>, t: Throwable) {
                        Log.e("QnaFragment", "Failed to searchView")
                    }
                })
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
    }
}