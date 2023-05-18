package com.example.coding_study

import retrofit2.Call
import retrofit2.http.GET

interface MyPageGetService {
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