package com.example.coding_study.common

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class TokenManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("MyToken", Context.MODE_PRIVATE)
    private val sharedPreferencesRefreshToken = context.getSharedPreferences("MyRefreshToken", Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) { // 액세스 토큰 저장 함수
        val editor = sharedPreferences.edit()
        editor.putString("token", token)
        if (!editor.commit()) {
            Log.e("saveAccessToken", "Failed to save token")
        }
    }

    fun saveRefreshToken(refreshToken: String) { // 리프레쉬 토큰 저장 함수
        val editor = sharedPreferencesRefreshToken.edit()
        editor.putString("refreshToken", refreshToken)
        if (!editor.commit()) {
            Log.e("saveRefreshToken", "Failed to save refreshToken")
        }
    }

    fun getAccessToken(): String { // 액세스 토큰 반환
        return sharedPreferences.getString("token", "") ?: ""
    }

    fun getRefreshToken(): String { // 리프레쉬 토큰 반환
        return sharedPreferencesRefreshToken.getString("refreshToken", "") ?: ""
    }

    fun checkAccessTokenExpiration() { // 액세스 토큰 유효기간 확인
        val accessToken = getAccessToken()
        try {
            val decodedJWT: DecodedJWT = JWT.decode(accessToken) // accessToken을 디코딩하여 decodedJWT 객체 생성
            val expirationTime = decodedJWT.expiresAt?.time ?: 0 // accessToken의 만료시간을 expirationTime에 저장
            Log.e("Token Manager access Token Decoding expirationTime", expirationTime.toString())

            val currentTime = Date().time
            if (currentTime >= expirationTime) {
                // 액세스 토큰 만료됨, 서버에 액세스 토큰과 리프레시 토큰 보내기
                val refreshToken = getRefreshToken() // 리프레시 토큰 가져오기
                sendTokensToServer(accessToken, refreshToken)
            }
        } catch (e: Exception) {
            Log.e("checkAccessTokenExp", "토큰 디코딩에 실패하였습니다: ${e.message}")
        }
    }

    private fun sendTokensToServer(accessToken: String, refreshToken: String) {
        // 서버에 액세스 토큰과 리프레시 토큰을 전송 (액세스 토큰 만료시 호출됨)
        Log.e("sendToknesToSever", accessToken)

        val retrofitBearer = Retrofit.Builder()
            .baseUrl("http://52.79.53.62:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer $accessToken")
                            .build()
                        Log.d("TokenManager_sendTokensToServer", "Token: $accessToken")
                        chain.proceed(request)
                    }
                    .build()
            )
            .build()

        val tokenService = retrofitBearer.create(TokenService::class.java)
        val tokenRequest = Token(accessToken, refreshToken)

        tokenService.requestToken(tokenRequest).enqueue(object :Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    Log.e("TokenManager tokenService response body", tokenResponse.toString())

                    if (tokenResponse != null) {
                        if (tokenResponse.result) { // result가 true일 경우 새로운 access token 발급
                            val newAccessToken = tokenResponse.data
                            if (newAccessToken != null) {
                                saveAccessToken(newAccessToken) // 새로 발급받은 access token 저장
                            }
                        } else { // result가 false일 경우 refresh token이 만료됐으므로 로그인 화면으로 이동, 다시 로그인

                        }
                    }
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Log.e("sendTokensToServer tokenService error", "${t.message}")
            }
        })
    }
}