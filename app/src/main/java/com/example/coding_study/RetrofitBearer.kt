package com.example.coding_study

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
/*
class RetrofitBearer(private val context: Context) {
    val sharedPreferences = context.getSharedPreferences("MyToken", Context.MODE_PRIVATE)
    val token = sharedPreferences?.getString("token", "") // 저장해둔 토큰값 가져오기

    companion object {
        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://112.154.249.74:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + instance.token.orEmpty())
                            //.addHeader("Authorization", "Bearer $token")
                            .build()
                        Log.d("TokenInterceptor_StudyFragment", "Token: " + instance.token.orEmpty())
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        lateinit var instance: RetrofitBearer

        fun init(context: Context) {
            instance = RetrofitBearer(context)
        }
    }
}

 */


