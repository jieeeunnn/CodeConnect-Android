package com.example.coding_study

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.coding_study.databinding.DeleteDialogBinding
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CommentDeleteDialog(id:Long) : DialogFragment() {
    private lateinit var binding: DeleteDialogBinding
    private var id : Long = 0

    init {
        this.id = id
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DeleteDialogBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경 투명
        binding.dialogTextView.text = "댓글을 삭제하시겠습니까?"

        binding.dialogNoButton.setOnClickListener {
            dismiss()
        }

        binding.dialogYesButton.setOnClickListener {
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
                            Log.d("TokenInterceptor_StudyDeleteFragment", "Token: " + token.orEmpty())
                            chain.proceed(request)
                        }
                        .build()
                )
                .build()


            val commentDeleteService = retrofitBearer.create(CommentDeleteService::class.java)

            commentDeleteService.commentDeletePost(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.e("QnaComment Delete_response code", "${response.code()}")
                        dismiss()
                        Toast.makeText(context, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(context, "게시글 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }

            })


        }

        return binding.root
        }
}