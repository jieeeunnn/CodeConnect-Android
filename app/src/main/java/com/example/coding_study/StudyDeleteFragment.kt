package com.example.coding_study

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class StudyDeleteFragment : DialogFragment() { // 게시글 삭제 여부 다이얼로그
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        //저장된 토큰값 가져오기
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

        val sharedPreferencesHostId = requireActivity().getSharedPreferences("MyHostIds", Context.MODE_PRIVATE)
        val hostIds = sharedPreferencesHostId?.getLong("MyHostIds",0)

        val studyDeleteService = retrofitBearer.create(StudyDeleteService::class.java)

        return AlertDialog.Builder(requireContext()).apply {
            setTitle("게시글 삭제")
            setMessage("게시글을 삭제 하시겠습니까?")
            setPositiveButton("예") {dialog, id ->

                if (hostIds != null) {
                    studyDeleteService.deletePost(hostIds).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                            if (response.isSuccessful) {
                                Log.e("StudyHostFragment Delete_response code", "is : ${response.code()}") // 서버 응답 코드 log 출력

                                //dismiss()

                                Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()


                                //글 삭제 후 스터디 게시판으로 돌아감
                                //requireActivity().supportFragmentManager.popBackStack()
                                /*
                                val parentFragment = parentFragment
                                if (parentFragment is StudyFragment) {
                                    requireActivity().supportFragmentManager.popBackStack()
                                }
                                 */

                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            // 삭제 요청에 대한 예외 처리
                            Toast.makeText(context, "게시글 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                println("JoinDialogFragment 확인")}

            setNegativeButton("아니오") { dialog, id ->
                println("StudyHostFragment Delete 취소")
                dismiss()
            }
        }.create()
    }
}