package com.example.coding_study

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

//output
//서버를 호출했을 때 받아오는 응답 값
data class LoginResponse (
    //변수명이 JSON에 있는 키값과 같아야함
    var result: Boolean,
    //var email : String,
    //var password : String,
    //val Member: User? // 로그인 정보가 틀렸거나 회원 정보가 없는 경우 User 객체는 null이 됨
    var message: String,
    var data: SignInResponseDto?
    )

data class SignInResponseDto (
    val token: String,
    val exprTime: Int,
    val member: Member
)

data class Member (
    var email: String = "",
    var password: String = "",
    var nickname: String = "",
    var createMemberTime : String = "",
    var address : String = "",
    var fieldList: List<String>
)


//input

interface LoginService {
    @POST("members/signIn")
    fun requestLogin(@Body loginrequest: LoginRequest): Call<LoginResponse>
}

/*
interface LoginService {
    @GET("members/signIn")
    fun requestLogin(
        @Query("email") email: String,
        @Query("password") password: String
    ): Call<LoginResponse>
}
 */


// 요청 데이터
data class LoginRequest(
    val email: String,
    val password: String
    )


