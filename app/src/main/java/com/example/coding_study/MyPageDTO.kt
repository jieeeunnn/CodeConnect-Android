package com.example.coding_study

import androidx.room.Delete
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MyPageGetService { // 마이페이지 정보 조회
    @GET("profile/userinfo/{nickname}")
    fun myPageGetProfile(
        @Path("nickname") nickname: String
    ): Call<MyPageProfileResponse>
}

data class MyPageProfileResponse (
    var result: Boolean,
    var message: String,
    var data: MyProfile
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
    @PUT("profile/update")
    fun myPageEditPost(@Body myPageEdit: MyPageEdit) : Call<MyPageEditResponse>
}
data class MyPageEditResponse (
    var result: Boolean,
    var message: String,
    var data: MyPageEdit
        )

data class MyPageEdit (
    var nickname: String,
    var address: String,
    var fieldList: List<String>
        )





interface MyPageMyStudyService { // 마이페이지 내가 쓴 스터디 게시글 조회
    @GET("profile/userRecruitment/{nickname}")
    fun myStudyGetList(
        @Path("nickname") nickname: String
    ):Call<StudyListResponse>
}


interface MyPageMyQnaService{ // 마이페이지 내가 쓴 Qna 게시글 조회
    @GET("profile/userQna/{nickname}")
    fun myQnaGetList(
        @Path("nickname") nickname: String
    ): Call<QnaListResponse>
}


interface MyPageDeleteMemberService { // 회원 탈퇴
    @DELETE("profile/delete")
    fun memberDelete(): Call<Void>
}