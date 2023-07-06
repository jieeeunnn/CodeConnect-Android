package com.example.coding_study.common

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.*

//output
//서버를 호출했을 때 받아오는 응답 값
data class LoginResponse (
    //변수명이 JSON에 있는 키값과 같아야함
    var result: Boolean,
    var message: String,
    var data: SignInResponseDto?
    )

data class SignInResponseDto (
    val accessToken: String, // 기존 토큰 (액세스 토큰)
    val refreshToken: String, // 리프레쉬 토큰
    val exprTime: Int,
    var address: String,
    var nickname: String,
    var fieldList: List<String>
)


interface LoginService {
    @POST("members/login")
    fun requestLogin(@Body loginrequest: LoginRequest): Call<LoginResponse>
}

// 요청 데이터
data class LoginRequest(
    val email: String,
    val password: String
    )


