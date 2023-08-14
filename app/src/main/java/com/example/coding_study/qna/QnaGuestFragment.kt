package com.example.coding_study.qna

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.common.LoadImageTask
import com.example.coding_study.R
import com.example.coding_study.common.TokenManager
import com.example.coding_study.databinding.QnaGuestBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QnaGuestFragment : Fragment(R.layout.qna_guest) {
    private lateinit var binding: QnaGuestBinding
    private lateinit var qnaCommentAdapter: QnaCommentAdapter
    private val tokenManager: TokenManager by lazy { TokenManager(requireContext()) }
    private val token: String by lazy { tokenManager.getAccessToken() }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QnaGuestBinding.inflate(inflater, container, false)

        // 가져온 qnaRecruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaGuestRecruitmentJson")
        val commentHostJson = arguments?.getString("commentHostJson")
        val commentGuestJson = arguments?.getString("commentGuestJson")

        Log.e("QnaGuestFragment adapter ", "$qnaJson, $commentHostJson")

        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)
        val commentHost = qnaGson.fromJson<List<Comment>>(commentHostJson, object : TypeToken<List<Comment>>() {}.type)
        val commentGuest = qnaGson.fromJson<List<Comment>>(commentGuestJson, object : TypeToken<List<Comment>>() {}.type)

        var commentList: List<Comment> = ((commentHost ?: emptyList()) + (commentGuest ?: emptyList())).sortedBy { it.currentDateTime }

        if (commentHost != null || commentGuest != null) {
            val qnaGuestRecyclerView = binding.qnaGuestRecyclerView
            qnaCommentAdapter = QnaCommentAdapter(fragmentManager = childFragmentManager, commentList, tokenManager)
            qnaGuestRecyclerView.adapter = qnaCommentAdapter
            binding.qnaGuestRecyclerView.layoutManager = LinearLayoutManager(context)

            qnaCommentAdapter.notifyDataSetChanged()
        }else{
            commentList = emptyList()
            qnaCommentAdapter = QnaCommentAdapter(childFragmentManager, commentList, tokenManager)
            binding.qnaGuestRecyclerView.adapter = qnaCommentAdapter
            binding.qnaGuestRecyclerView.layoutManager = LinearLayoutManager(context)

            qnaCommentAdapter.notifyDataSetChanged()
        }

        binding.qnaGuestNickname.text = qnaRecruitment.nickname
        binding.qnaGuestTitle.text = qnaRecruitment.title
        binding.qnaGuestContent.text = qnaRecruitment.content
        binding.qnaGuestCurrentTime.text = qnaRecruitment.currentDateTime
        binding.guestLikeCountView.text = qnaRecruitment.likeCount.toString()

        val profileImageUrl: String = "http://52.79.53.62:8080/"+ qnaRecruitment.profileImagePath // 프로필 이미지
        val profileImageView: ImageView = binding.qnaGuestProfileImage
        val profileLoadImageTask = LoadImageTask(profileImageView, token)
        profileLoadImageTask.execute(profileImageUrl)

        val imageUrl: String? = "http://52.79.53.62:8080/"+ "${qnaRecruitment.imagePath}" // 게시글에 이미지 첨부 시
        val imageView: ImageView = binding.qnaGuestImageView
        val loadImageTask = LoadQnaImageTask(imageView, token)
        loadImageTask.execute(imageUrl)

        binding.qnaGuestSwifeRefreshLayout.setOnRefreshListener {
            loadQnaGuest()
        }

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://52.79.53.62:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                        Log.d("TokenInterceptor", "Token: $token")
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val isLiked = qnaRecruitment.liked

        if (isLiked) { // 좋아요가 눌린 상태
            binding.noHeartImage.visibility = View.GONE
            binding.onHeartImage.visibility = View.VISIBLE
        } else { // 좋아요 취소 상태
            binding.noHeartImage.visibility = View.VISIBLE
            binding.onHeartImage.visibility = View.GONE
        }

        val qnaId = qnaRecruitment.qnaId

        val qnaHeartService = retrofitBearer.create(QnaHeartService::class.java)

        binding.noHeartImage.setOnClickListener {// 좋아요 누를 때
            tokenManager.checkAccessTokenExpiration() // 액세스 토큰 유효기간 확인

            qnaHeartService.qnaHeartPut(qnaId).enqueue(object : Callback<QnaHeart>{
                override fun onResponse(call: Call<QnaHeart>, response: Response<QnaHeart>) {
                    if (response.isSuccessful) {
                        Log.e("QnaHost heart Count response code", "${response.code()}")
                        Log.e("QnaHost heart Count response body", "${response.body()}")

                        val heartResponse = response.body()
                        val heartCount = heartResponse?.data?.likeCount
                        val isLiked = heartResponse?.data?.liked

                        if(isLiked == true) {
                            binding.noHeartImage.visibility = View.GONE
                            binding.onHeartImage.visibility = View.VISIBLE
                        }

                        binding.guestLikeCountView.text = heartCount.toString()
                    }
                }

                override fun onFailure(call: Call<QnaHeart>, t: Throwable) {
                    Toast.makeText(context, "qna heart put 서버 연결 실패", Toast.LENGTH_LONG).show()
                }
            })
        }

        binding.onHeartImage.setOnClickListener { // 좋아요 취소 시
            tokenManager.checkAccessTokenExpiration() // 액세스 토큰 유효기간 확인

            qnaHeartService.qnaHeartPut(qnaId).enqueue(object : Callback<QnaHeart>{
                override fun onResponse(call: Call<QnaHeart>, response: Response<QnaHeart>) {
                    if (response.isSuccessful) {
                        Log.e("QnaHost heart Count response code", "${response.code()}")
                        Log.e("QnaHost heart Count response body", "${response.body()}")

                        val heartResponse = response.body()
                        val heartCount = heartResponse?.data?.likeCount
                        val isLiked = heartResponse?.data?.liked

                        if (isLiked == false) {
                            binding.noHeartImage.visibility = View.VISIBLE
                            binding.onHeartImage.visibility = View.GONE
                        }

                        binding.guestLikeCountView.text = heartCount.toString()
                    }
                }

                override fun onFailure(call: Call<QnaHeart>, t: Throwable) {
                    Toast.makeText(context, "qna heart put 서버 연결 실패", Toast.LENGTH_LONG).show()
                }

            })
        }

        val qnaCommentCreateService = retrofitBearer.create(QnaCommentCreateService::class.java)

        binding.guestCommentButton.setOnClickListener { // 댓글 작성 버튼
            tokenManager.checkAccessTokenExpiration() // 액세스 토큰 유효기간 확인

            val comment = binding.guestCommentEdit.text.toString()
            val qnaCommentRequest = QnaCommentRequest(comment)

            qnaCommentCreateService.qnaCommentCreate(qnaId, qnaCommentRequest).enqueue(object : Callback<QnaCommentResponse>{
                override fun onResponse(call: Call<QnaCommentResponse>, response: Response<QnaCommentResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.e("Qna post comment response code", "${response.code()}")
                        Log.e("Qna post comment response body", "${response.body()}")
                        binding.guestCommentEdit.text = null

                        loadQnaGuest()
                    }
                }

                override fun onFailure(call: Call<QnaCommentResponse>, t: Throwable) {
                    Toast.makeText(context, "qna comment 서버 연결 실패", Toast.LENGTH_LONG).show()
                }
            })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val parentFragment = parentFragment
        if (parentFragment is QnAFragment) {
            parentFragment.hideFloatingButton()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()

            val parentFragment = parentFragment
            if (parentFragment is QnAFragment) {
                parentFragment.showFloatingButton()
            }
        }
    }

    fun loadQnaGuest() {
        tokenManager.checkAccessTokenExpiration()

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://52.79.53.62:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                        Log.d("TokenInterceptor_StudyFragment", "Token: $token")
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaGuestRecruitmentJson")
        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)

        val qnaId = qnaRecruitment.qnaId
        val qnaOnlyService = retrofitBearer.create(QnaOnlyService::class.java)

        qnaOnlyService.qnaGetOnlyPost(qnaId).enqueue(object : Callback<QnaOnlyResponse>{
            override fun onResponse(
                call: Call<QnaOnlyResponse>,
                response: Response<QnaOnlyResponse>
            ) {
                val qnaOnlyResponse = response.body()
                Log.e("QnaOnlyResponse_reponse.body", "is : $qnaOnlyResponse")
                Log.e("QnaOnlyResponse_response.code", "is : ${response.code()}")

                if (qnaOnlyResponse?.result == true && qnaOnlyResponse.data.containsKey(QnaRole.GUEST)) {
                    val qnaGuest = qnaOnlyResponse.data[QnaRole.GUEST] as Any
                    val qnaUploadDto = qnaGson.fromJson(qnaGson.toJson(qnaGuest), QnaUploadDto::class.java)

                    binding.qnaGuestNickname.text = qnaUploadDto.nickname
                    binding.qnaGuestTitle.text = qnaUploadDto.title
                    binding.qnaGuestContent.text = qnaUploadDto.content
                    binding.qnaGuestCurrentTime.text = qnaUploadDto.currentDateTime
                    binding.guestLikeCountView.text = qnaRecruitment.likeCount.toString()

                    val imageUrl: String? = "http://52.79.53.62:8080/"+ "${qnaUploadDto.profileImagePath}"
                    val imageView: ImageView = binding.qnaGuestProfileImage
                    val loadImageTask = LoadImageTask(imageView, token)
                    loadImageTask.execute(imageUrl)

                    val commentHost = qnaOnlyResponse.data[QnaRole.COMMENT_HOST] as? List<Comment>
                    val commentGuest = qnaOnlyResponse.data[QnaRole.COMMENT_GUEST] as? List<Comment>
                    Log.e("QnaHostFragment LoadQnaHost commentHost", "$commentHost")
                    Log.e("QnaHostFragment LoadQnaHost commentGuest", "$commentGuest")

                    val commentHostJson = qnaGson.toJson(commentHost)
                    val commentGuestJson = qnaGson.toJson(commentGuest)

                    val commentHostList = qnaGson.fromJson<List<Comment>>(commentHostJson, object : TypeToken<List<Comment>>() {}.type)
                    val commentGuestList = qnaGson.fromJson<List<Comment>>(commentGuestJson, object : TypeToken<List<Comment>>() {}.type)

                    var commentList: List<Comment> = ((commentHostList ?: emptyList()) + (commentGuestList ?: emptyList())).sortedBy { it.currentDateTime }

                    qnaCommentAdapter.commentList = commentList
                    qnaCommentAdapter.notifyDataSetChanged()

                    binding.qnaGuestSwifeRefreshLayout.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<QnaOnlyResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

    }
}