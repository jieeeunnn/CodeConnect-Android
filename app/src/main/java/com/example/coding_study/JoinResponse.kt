package com.example.coding_study

import android.provider.ContactsContract
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

//서버를 호출했을 때 받아오는 응답 값
data class JoinResponse (
    //변수명이 JSON에 있는 키값과 같아야함
    var result: Boolean,
    var message: String,
    var data: member? // 로그인 정보가 틀렸거나 회원 정보가 없는 경우 member 객체는 null이 됨
)

//input
interface JoinService {
    @POST("members/signUp")
    fun requestJoin(@Body joinrequest: JoinRequest): Call<JoinResponse>
}

// 요청 데이터
data class JoinRequest(
    val email: String,
    val password: String,
    val passwordCheck: String,
    val nickname: String,
    val state: String,
    val city: String,
    val field: String
)




