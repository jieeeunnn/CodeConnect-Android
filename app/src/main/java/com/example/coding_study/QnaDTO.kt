package com.example.coding_study

import android.icu.text.CaseMap.Title
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// 게시글 작성 시 응답값
data class QnaResponse ( // qnaUpload에서 사용
    var result: Boolean,
    var message: String,
    var data: QnaUploadDto
)

data class QnaUploadDto(
    var title: String,
    var content: String,
    var nickname: String,
    var currentDateTime: String,
    var modifiedDateTime: String,
    var qnaId: Int,
    var commentCount: Int
)


interface QnaService { // qna 글 업로드 인터페이스, qnaUpload에서 사용
    @POST("qna/create")
    fun requestQna(@Body qnarequest: QnaRequest): Call<QnaResponse>
}

data class QnaRequest( // 게시글 작성 시 전송값, qnaUpload에서 사용
    var title: String,
    var content: String
)


interface QnaGetService { // qna 게시글 조회 인터페이스
    @GET("qna/list") // 전체 게시글
    fun qnagetList(
    ): Call<QnaListResponse>
}

data class QnaListResponse ( // 게시글 응답값 (qna 게시판에서 게시글 전체 불러오기)
    var result: Boolean,
    var message: String,
    var data: List<QnaUploadDto>? // 게시글 데이터를 리스트로 받음
)