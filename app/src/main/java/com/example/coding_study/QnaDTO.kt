package com.example.coding_study

import com.google.gson.Gson
import retrofit2.Call
import retrofit2.http.*

// 게시글 작성 시 응답값
data class QnaResponse ( // qnaUpload에서 사용, qnaEdit에서 사용(댓글 구현하고 edit에서는 댓글도 받아야함. edit 콜백 수정하기)
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
    var data: Map<QnaRole, Any> // 서버에서 Role-게시물 정보를 Map으로 전달해줌
)

enum class QnaRole{
    GUEST, // data: QnaUploadDto
    HOST, // data: QnaUploadDto
    COMMENT_GUEST, // data: List<Comment>
    COMMENT_HOST // data: List<Comment>
}

data class QnaCommentListResponse (
    var comments: List<Comment>
    )




interface QnaEditService { // qna 게시글 수정
    @PUT("qna/update/{qnaId}")
    fun qnaEditPost(@Path("qnaId") id:Long, @Body qnaEdit: QnaRequest): Call<QnaResponse>
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




//comment api
interface QnaCommentCreateService { // 댓글 작성
    @POST("comment/create/{qnaId}")
    fun qnaCommentCreate(@Path("qnaId") qnaPostId: Long ,@Body qnaCommentRequest : QnaCommentRequest) : Call<QnaCommentResponse>
}

data class QnaCommentRequest( // 댓글 작성시 전송값
    var comment: String
)

data class QnaCommentResponse ( // 댓글 작성시 반환값
    var result: Boolean,
    var message: String,
    var data: Comment
)

data class Comment (
    val commentId: Long,
    val comment: String,
    val nickname: String,
    val currentDateTime: String,
    val modifiedDateTime: Any?,
    val cocommentCount: Long,
    var commentRole: QnaRole
)




interface CommentDeleteService { // 댓글 삭제
    @DELETE("comment/delete/{commentId}")
    fun commentDeletePost(@Path("commentId") id: Long): Call<Void>
}