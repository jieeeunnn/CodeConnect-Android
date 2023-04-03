package com.example.coding_study

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

//서버를 호출했을 때 받아오는 응답 값
data class StudyResponse (
    //변수명이 JSON에 있는 키값과 같아야함
    var result: Boolean,
    var message: String,
    var data: RecruitmentCreateDto?
    )

data class RecruitmentCreateDto (
    var title: String = "",
    var content: String = "",
    var nickname: String = "",
    var currentDateTime : String = "",
    var modifiedDataTime : String = "",
    var recruitmentId : Long, // 게시글 번호
    var address: String ="",
    var count : Int,
    var role: Role,
    var field: String
)

//input
interface StudyService {
    @POST("recruitment/create")
    fun requestStudy(@Body studyrequest: StudyRequest): Call<StudyResponse>
}

// 요청 데이터
data class StudyRequest(
    val title: String,
    val content: String,
    val count: Int,
    var role : Role,
    var field : String
)

enum class Role(val role: String) {
    HOST("HOST"),
    GUEST("GUEST")
}