package com.example.coding_study

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.coding_study.databinding.WriteQnaBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream


class QnaUpload : Fragment() {
    private lateinit var binding: WriteQnaBinding
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WriteQnaBinding.inflate(inflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { // secondActivity의 onBackPressed 함수 콜백
            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStack()

            val parentFragment = parentFragment
            if (parentFragment is QnAFragment) {
                parentFragment.showFloatingButton()
            }
        }

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data // 선택된 이미지를 여기서 처리
                selectedImageUri = intent?.data // 선택된 이미지 URI를 필요에 따라 처리

                displaySelectedImage()
            }
        }

        binding.qnaImageButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(galleryIntent)
        }

        displaySelectedImage()

        val contentResolver: ContentResolver = requireContext().contentResolver
        val inputStream: InputStream? = selectedImageUri?.let { contentResolver.openInputStream(it) }
        val imageBytes: ByteArray = inputStream?.readBytes() ?: ByteArray(0)

        val base64Image: String = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        // 서버로 base64Image를 전송하는 로직을 추가하면 됩니다.


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
                            //.addHeader("Authorization", "Bearer $token")
                            .build()
                        Log.d("TokenInterceptor", "Token: " + token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val qnaService = retrofitBearer.create(QnaService::class.java)

        binding.qnaButtonUpload.setOnClickListener {
            val title = binding.qnaEditTitle.text.toString()
            val content = binding.qnaEditContent.text.toString()

            val qnaRequest = QnaRequest(title, content, base64Image)

            qnaService.requestQna(qnaRequest).enqueue(object : Callback<QnaResponse> {
                override fun onResponse(call: Call<QnaResponse>, response: Response<QnaResponse>) {
                    Log.e("Qna Upload response code", "is : ${response.code()}")

                    if (response.isSuccessful) {
                        val qnaResponse = response.body() // 서버에서 받아온 응답 데이터
                        Log.e("QnaPost" , "is : $qnaResponse")
                    }
                }

                override fun onFailure(call: Call<QnaResponse>, t: Throwable) {
                    Toast.makeText(context, "통신에 실패했습니다", Toast.LENGTH_LONG).show()
                }
            })
            //업로드 후 qna 게시판으로 돌아감

            val parentFragment = parentFragment
            if (parentFragment is QnAFragment) {
                parentFragment.showFloatingButton()
                parentFragment.onResume()
            }

            val parentFragmentManager = requireActivity().supportFragmentManager
            parentFragmentManager.popBackStackImmediate()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val parentFragment = parentFragment
        if (parentFragment is QnAFragment) {
            parentFragment.hideFloatingButton()
        }
    }

    private fun displaySelectedImage() {
        val imageView = binding.qnaImageView
        if (selectedImageUri != null) {
            val glideRequest = Glide.with(this)
                .load(selectedImageUri)

            // 이미지를 원하는 크기로 제한
            val targetWidth = 300 // 원하는 가로 크기
            val targetHeight = 300 // 원하는 세로 크기
            glideRequest.override(targetWidth, targetHeight)

            glideRequest.into(imageView)
        } else {
            // 이미지가 없는 경우, ImageView를 빈 상태로 남겨둠
            imageView.setImageDrawable(null)
        }
    }
}