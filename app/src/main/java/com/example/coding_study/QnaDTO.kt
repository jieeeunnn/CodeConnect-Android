package com.example.coding_study

import retrofit2.Call
import retrofit2.http.*

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
    var qnaId: Long,
    var commentCount: Int,
    var comments: List<Comment>
)




interface QnaService { // qna 글 업로드 인터페이스, qnaUpload에서 사용
    @POST("qna/create")
    fun requestQna(@Body qnaRequest: QnaRequest): Call<QnaResponse>
}

data class QnaRequest( // 게시글 작성 시 전송값, qnaUpload에서 사용
    var title: String,
    var content: String
)




interface QnaGetService { // qna 게시글 조회 인터페이스
    @GET("qna/list") // 전체 게시글
    fun qnaGetList(
    ): Call<QnaListResponse>
}

data class QnaListResponse ( // 게시글 응답값 (qna 게시판에서 게시글 전체 불러오기)
    var result: Boolean,
    var message: String,
    var data: List<QnaUploadDto>? // 게시글 데이터를 리스트로 받음
)





interface QnaOnlyService { // 게시글 하나만 조회 인터페이스
    @GET("qna/detail/{qnaId}")
    fun qnaGetOnlyPost(
        @Path("qnaId") qnaPostId: Long
    ): Call<QnaOnlyResponse>
}

data class QnaOnlyResponse( // 게시글 하나만 조회할 때 응답값 (Map으로 Role 정보 받음)
    var result: Boolean,
    var message: String,
    var data: Map<QnaRole, QnaUploadDto> // 서버에서 Role-게시물 정보를 Map으로 전달해줌
)

enum class QnaRole{
    GUEST,
    HOST
}




interface QnaSearchService { // qna 게시판 검색 api
    @GET("qna/search/{text}")
    fun qnaSearch(
        @Path("text") text: String
    ): Call<QnaListResponse>
}

interface QnaDeleteService { // qna 게시글 삭제
    @DELETE("qna/delete/{qnaId}")
    fun qnaDeletePost(@Path("qnaId") id: Long): Call<Void>
}


interface QnaCommentCreateService {
    @POST("comment/create/{qnaId}")
    fun qnaCommentCreate(@Path("qnaId") qnaPostId: Long ,@Body qnaCommentRequest : QnaCommentRequest) : Call<QnaCommentResponse>
}

data class QnaCommentRequest(
    var comment: String
)

data class QnaCommentResponse ( // qnaUpload에서 사용
    var result: Boolean,
    var message: String,
    var data: Comment
)

data class Comment (
    var commentId: Long,
    var comment: String,
    var nickname: String,
    var currentDateTime: String
)