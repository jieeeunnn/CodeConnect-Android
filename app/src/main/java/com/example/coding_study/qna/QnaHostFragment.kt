package com.example.coding_study.qna

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.*
import com.example.coding_study.common.LoadImageTask
import com.example.coding_study.common.TokenManager
import com.example.coding_study.databinding.QnaHostBinding
import com.example.coding_study.dialog.DeleteDialog
import com.example.coding_study.dialog.DeleteDialogInterface
import com.example.coding_study.mypage.MyPageMyQna
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

open class QnaHostFragment : Fragment(R.layout.qna_host), DeleteDialogInterface {
    private lateinit var binding: QnaHostBinding
    private lateinit var qnaCommentAdapter: QnaCommentAdapter
    private val tokenManager: TokenManager by lazy { TokenManager(requireContext()) }
    private val token: String by lazy { tokenManager.getAccessToken() }

    fun onBackPressed() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QnaHostBinding.inflate(inflater, container, false)

        // 가져온 qnaRecruitment 정보를 사용해서 레이아웃에 표시하는 코드 작성
        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaHostRecruitmentJson")
        val commentHostJson = arguments?.getString("commentHostJson")
        val commentGuestJson = arguments?.getString("commentGuestJson")

        Log.e("QnaHostFragment qnaJson ", "$qnaJson, $commentHostJson")

        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)
        val commentHost = qnaGson.fromJson<List<Comment>>(commentHostJson, object : TypeToken<List<Comment>>() {}.type)
        val commentGuest = qnaGson.fromJson<List<Comment>>(commentGuestJson, object : TypeToken<List<Comment>>() {}.type)

        var commentList: List<Comment> = ((commentHost ?: emptyList()) + (commentGuest ?: emptyList())).sortedBy { it.currentDateTime }

        if (commentHost != null || commentGuest != null) {
            val qnaHostRecyclerView = binding.qnaHostRecyclerView
            qnaCommentAdapter = QnaCommentAdapter( fragmentManager = childFragmentManager, commentList, tokenManager)
            qnaHostRecyclerView.adapter = qnaCommentAdapter
            binding.qnaHostRecyclerView.layoutManager = LinearLayoutManager(context)

            qnaCommentAdapter.notifyDataSetChanged()
        } else {
            commentList = emptyList()
            qnaCommentAdapter = QnaCommentAdapter(childFragmentManager, commentList, tokenManager)
            binding.qnaHostRecyclerView.adapter = qnaCommentAdapter
            binding.qnaHostRecyclerView.layoutManager = LinearLayoutManager(context)

            qnaCommentAdapter.notifyDataSetChanged()
        }

        binding.qnaHostNickname.text = qnaRecruitment.nickname
        binding.qnaHostTitle.text = qnaRecruitment.title

        val htmlString = qnaRecruitment.content // qnaRecruitment.content에는 HTML 형식의 문자열이 들어있다고 가정
        binding.qnaHostContent.text = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)

        //binding.qnaHostContent.text = qnaRecruitment.content
        binding.qnaHostCurrentTime.text = qnaRecruitment.currentDateTime
        binding.likeCountTextView.text = qnaRecruitment.likeCount.toString()

        val profileImageUrl: String = "http://52.79.53.62:8080/"+ qnaRecruitment.profileImagePath // 프로필 사진 띄우기
        val profileImageView: ImageView = binding.qnaHostProfileImage
        val profileLoadImageTask = LoadImageTask(profileImageView,token)
        profileLoadImageTask.execute(profileImageUrl)

        val imageUrl: String? = "http://52.79.53.62:8080/"+ "${qnaRecruitment.imagePath}" // 게시글에 사진 업로드
        val imageView: ImageView = binding.qnaHostImageView
        val loadImageTask = LoadQnaImageTask(imageView, token)
        loadImageTask.execute(imageUrl)

        binding.qnaHostSwifeRefreshLayout.setOnRefreshListener {
            loadQnaHost()
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
                        Log.d("TokenInterceptor_StudyFragment", "Token: $token")
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        binding.heartImageView.visibility = View.GONE
        binding.heartOnImage.visibility = View.GONE

        val isLiked = qnaRecruitment.liked
        Log.e("QnaHost isLiked", isLiked.toString())
        if (isLiked == true) { // 좋아요가 눌린 상태
            binding.heartImageView.visibility = View.GONE
            binding.heartOnImage.visibility = View.VISIBLE
        } else { // 좋아요 취소 상태
            binding.heartImageView.visibility = View.VISIBLE
            binding.heartOnImage.visibility = View.GONE
        }

        val qnaId = qnaRecruitment.qnaId

        val qnaHeartService = retrofitBearer.create(QnaHeartService::class.java)

        binding.heartImageView.setOnClickListener {// 좋아요 누를 때
            tokenManager.checkAccessTokenExpiration() // 액세스 토큰 유효기간 확인

                qnaHeartService.qnaHeartPut(qnaId).enqueue(object : Callback<QnaHeart> {
                    override fun onResponse(call: Call<QnaHeart>, response: Response<QnaHeart>) {
                        if (response.isSuccessful) {
                            Log.e("QnaHost heart Count response code", "${response.code()}")
                            Log.e("QnaHost heart Count response body", "${response.body()}")

                            val heartResponse = response.body()
                            val heartCount = heartResponse?.data?.likeCount
                            val isLiked = heartResponse?.data?.liked

                            if (isLiked == true) {
                                binding.heartImageView.visibility = View.GONE
                                binding.heartOnImage.visibility = View.VISIBLE
                            }

                            binding.likeCountTextView.text = heartCount.toString()
                        }
                    }

                    override fun onFailure(call: Call<QnaHeart>, t: Throwable) {
                        Toast.makeText(context, "qna heart put 서버 연결 실패", Toast.LENGTH_LONG).show()
                    }
                })
        }

        binding.heartOnImage.setOnClickListener { // 좋아요 취소
            tokenManager.checkAccessTokenExpiration() // 액세스 토큰 유효기간 확인

                qnaHeartService.qnaHeartPut(qnaId).enqueue(object : Callback<QnaHeart> {
                    override fun onResponse(call: Call<QnaHeart>, response: Response<QnaHeart>) {
                        if (response.isSuccessful) {
                            Log.e("QnaHost heart Count response code", "${response.code()}")
                            Log.e("QnaHost heart Count response body", "${response.body()}")

                            val heartResponse = response.body()
                            val heartCount = heartResponse?.data?.likeCount
                            val isLiked = heartResponse?.data?.liked

                            if (isLiked == false) {
                                binding.heartImageView.visibility = View.VISIBLE
                                binding.heartOnImage.visibility = View.GONE
                            }
                            binding.likeCountTextView.text = heartCount.toString()
                        }
                    }

                    override fun onFailure(call: Call<QnaHeart>, t: Throwable) {
                        Toast.makeText(context, "qna heart put 서버 연결 실패", Toast.LENGTH_LONG).show()
                    }
                })
        }

        //qna 삭제 버튼
        binding.qnaHostDeleteButton.setOnClickListener {
            val deleteDialog = DeleteDialog(this, qnaRecruitment.qnaId, "게시글을 삭제하시겠습니까?")
            deleteDialog.isCancelable = false
            deleteDialog.show(this.childFragmentManager, "deleteDialog")
        }

        //qna 수정 버튼
        binding.qnaHostEditButton.setOnClickListener {
            val qnaEditFragment = QnaEditFragment()
            val qnaBundle = Bundle()
            qnaBundle.putString("qnaRecruitmentJson", qnaGson.toJson(qnaRecruitment))
            qnaEditFragment.arguments = qnaBundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.qna_host_fragment, qnaEditFragment)
                .addToBackStack(null)
                .commit()
        }

        val qnaCommentCreateService = retrofitBearer.create(QnaCommentCreateService::class.java)

        // 댓글 버튼 (댓글 전송)
        binding.hostCommentButton.setOnClickListener {
            tokenManager.checkAccessTokenExpiration() // 액세스 토큰 유효기간 확인

            val comment = binding.hostComment.text.toString()
            val qnaCommentRequest = QnaCommentRequest(comment)

            qnaCommentCreateService.qnaCommentCreate(qnaId, qnaCommentRequest).enqueue(object : Callback<QnaCommentResponse>{
                override fun onResponse(call: Call<QnaCommentResponse>, response: Response<QnaCommentResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.e("Qna post comment response code", "${response.code()}")
                        Log.e("Qna post comment response body", "${response.body()}")
                        binding.hostComment.text = null

                        loadQnaHost()
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
        super.onViewCreated(view, savedInstanceState)

        val parentFragment = parentFragment
        if (parentFragment is QnAFragment) {
            parentFragment.hideFloatingButton()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백

            val parentFragment = parentFragment
            if (parentFragment is QnAFragment) {
                val parentFragmentManager = requireActivity().supportFragmentManager
                parentFragmentManager.popBackStack()

                parentFragment.showFloatingButton()
            }
            else if (parentFragment is MyPageMyQna) {
                onBackPressed()
            }
        }
    }

    override fun onYesButtonClick(id: Long) { // 삭제 다이얼로그 확인 버튼 클릭시 게시글 삭제
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
                        Log.d("TokenInterceptor_StudyDeleteFragment", "Token: $token")
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val qnaDeleteService = retrofitBearer.create(QnaDeleteService::class.java)

        qnaDeleteService.qnaDeletePost(id).enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.e("QnaHostFragment Delete Id----------------------------", "$id")
                if (response.isSuccessful) {
                    Log.e("QnaHostFragment Delete_response code", "${response.code()}")

                    val parentFragment = parentFragment
                    if (parentFragment is QnAFragment) {
                        parentFragment.showFloatingButton()
                        parentFragment.onResume()
                    }

                    //글 삭제 후 스터디 게시판으로 돌아감
                    requireActivity().supportFragmentManager.popBackStack()

                    Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "게시글 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun loadQnaHost() {
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
        val qnaJson = arguments?.getString("qnaHostRecruitmentJson")
        val qnaRecruitment = qnaGson.fromJson(qnaJson, QnaUploadDto::class.java)

        val qnaId = qnaRecruitment.qnaId
        val qnaOnlyService = retrofitBearer.create(QnaOnlyService::class.java)

        qnaOnlyService.qnaGetOnlyPost(qnaId).enqueue(object : Callback<QnaOnlyResponse>{
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<QnaOnlyResponse>,
                response: Response<QnaOnlyResponse>
            ) {
                if (response.isSuccessful) {
                    val qnaOnlyResponse = response.body()
                    Log.e("QnaOnlyResponse_reponse.body", "is : $qnaOnlyResponse")
                    Log.e("QnaOnlyResponse_response.code", "is : ${response.code()}")

                    if (qnaOnlyResponse?.result == true && qnaOnlyResponse.data.containsKey(QnaRole.HOST)) {
                        val qnaHost = qnaOnlyResponse.data[QnaRole.HOST] as Any
                        val qnaUploadDto = qnaGson.fromJson(qnaGson.toJson(qnaHost), QnaUploadDto::class.java)

                        binding.qnaHostNickname.text = qnaUploadDto.nickname
                        binding.qnaHostTitle.text = qnaUploadDto.title

                        val htmlString = qnaRecruitment.content // qnaRecruitment.content에는 HTML 형식의 문자열이 들어있다고 가정
                        binding.qnaHostContent.text = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)

                        //binding.qnaHostContent.text = qnaUploadDto.content
                        binding.qnaHostCurrentTime.text = qnaUploadDto.currentDateTime
                        binding.likeCountTextView.text = qnaUploadDto.likeCount.toString()


                        val imageUrl: String? = "http://52.79.53.62:8080/"+ qnaUploadDto.profileImagePath // 프로필 사진 띄우기
                        val imageView: ImageView = binding.qnaHostProfileImage
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

                        binding.qnaHostSwifeRefreshLayout.isRefreshing = false

                    }
                }
            }
            override fun onFailure(call: Call<QnaOnlyResponse>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }
}
