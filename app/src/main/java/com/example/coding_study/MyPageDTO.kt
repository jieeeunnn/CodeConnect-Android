package com.example.coding_study

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface MyPageGetService { // 마이페이지 정보 조회
    @GET("/profile")
    fun myPageGetProfile(
    ): Call<MyPageProfileResponse>
}

data class MyPageProfileResponse (
    var result: Boolean,
    var message: String,
    var data: List<MyProfile>
        )

data class MyProfile (
    var address: String,
    var email: String,
    var fieldList: List<String>,
    var nickname: String,
    var role: MyPageRole
    )

enum class MyPageRole {
    HOST,
    GUEST
}




interface MyPageEditService { // 마이페이지 수정 api
    @PUT("")
    fun myPageEditPost(@Body myPageEdit: MyPageEdit) : Call<MyPageProfileResponse>
}

data class MyPageEdit (
    var nickname: String,
    var address: String,
    var fieldList: List<String>
        )