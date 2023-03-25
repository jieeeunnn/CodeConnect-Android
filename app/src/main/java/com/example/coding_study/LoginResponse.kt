package com.example.coding_study

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

//output
//서버를 호출했을 때 받아오는 응답 값
data class LoginResponse (
    //변수명이 JSON에 있는 키값과 같아야함
    var success: Boolean,
    var email : String,
    var password : String,
    val user: User? // 로그인 정보가 틀렸거나 회원 정보가 없는 경우 User 객체는 null이 됨
    )

data class User (
    var email: String = "",
    var password: String = "",
    var passwordCheck: String = "",
    var nickname: String = "",
    var createMemberTime: String = "",
    var address: String = "",
    var field: String = ""
)


//input
interface LoginService {
    //input 정의
    @FormUrlEncoded
    @POST("members/signIn") // root url 다음 url 입력 (서비스 부분 url) , HTTP POST를 메서드를 사용하여 서버에 요청을 보냄
    fun requestLogin (
        @Field("email") email:String, // value값 이름이 서버에서 받는 이름과 같아야함 (userid)
        @Field("password") password:String
    ) : Call<LoginResponse> // output 정의
}


