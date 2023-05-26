package com.example.coding_study

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coding_study.databinding.QnaHostBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

open class QnaHostFragment : Fragment(R.layout.qna_host), DeleteDialogInterface{
    private lateinit var binding: QnaHostBinding
    private lateinit var qnaCommentAdapter: QnaCommentAdapter

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

        val qnaRecruitment = qnaGson.fromJson(qnaJson,QnaUploadDto::class.java)
        val commentHost = qnaGson.fromJson<List<Comment>>(commentHostJson, object : TypeToken<List<Comment>>() {}.type)
        val commentGuest = qnaGson.fromJson<List<Comment>>(commentGuestJson, object : TypeToken<List<Comment>>() {}.type)


        var commentList: List<Comment> = ((commentHost ?: emptyList()) + (commentGuest ?: emptyList())).sortedBy { it.currentDateTime }

        if (commentHost != null || commentGuest != null) {
            val qnaHostRecyclerView = binding.qnaHostRecyclerView
            qnaCommentAdapter = QnaCommentAdapter( fragmentManager = childFragmentManager, commentList)
            qnaHostRecyclerView.adapter = qnaCommentAdapter
            binding.qnaHostRecyclerView.layoutManager = LinearLayoutManager(context)

            qnaCommentAdapter.notifyDataSetChanged()
        } else {
            commentList = emptyList()
            qnaCommentAdapter = QnaCommentAdapter(childFragmentManager, commentList)
            binding.qnaHostRecyclerView.adapter = qnaCommentAdapter
            binding.qnaHostRecyclerView.layoutManager = LinearLayoutManager(context)

            qnaCommentAdapter.notifyDataSetChanged()
        }

        binding.qnaHostNickname.text = qnaRecruitment.nickname
        binding.qnaHostTitle.text = qnaRecruitment.title
        binding.qnaHostContent.text = qnaRecruitment.content
        binding.qnaHostCurrentTime.text = qnaRecruitment.currentDateTime

        val profileImageUrl: String = "http://112.154.249.74:8080/"+ qnaRecruitment.profileImagePath // 프로필 사진 띄우기
        val profileImageView: ImageView = binding.qnaHostProfileImage
        val profileLoadImageTask = LoadImageTask(profileImageView)
        profileLoadImageTask.execute(profileImageUrl)

        val imageUrl: String? = "http://112.154.249.74:8080/"+ "${qnaRecruitment.imagePath}" // 게시글에 사진 업로드
        val imageView: ImageView = binding.qnaHostImageView
        val loadImageTask = LoadQnaImageTask(imageView)
        loadImageTask.execute(imageUrl)

        binding.qnaHostSwifeRefreshLayout.setOnRefreshListener {
            loadQnaHost()
        }

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
            val comment = binding.hostComment.text.toString()
            val qnaId = qnaRecruitment.qnaId
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
                            .build()
                        Log.d("TokenInterceptor_StudyDeleteFragment", "Token: " + token.orEmpty())
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


        val qnaGson = Gson()
        val qnaJson = arguments?.getString("qnaHostRecruitmentJson")
        val qnaRecruitment = qnaGson.fromJson(qnaJson,QnaUploadDto::class.java)

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
                        binding.qnaHostContent.text = qnaUploadDto.content
                        binding.qnaHostCurrentTime.text = qnaUploadDto.currentDateTime

                        val imageUrl: String? = "http://112.154.249.74:8080/"+ qnaUploadDto.profileImagePath // 프로필 사진 띄우기
                        val imageView: ImageView = binding.qnaHostProfileImage
                        val loadImageTask = LoadImageTask(imageView)
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
