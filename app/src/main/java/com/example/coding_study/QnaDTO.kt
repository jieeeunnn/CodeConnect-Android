package com.example.coding_study

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
    var qnaId: Long,
    var commentCount: Int,
    var imagePath: String?, // 게시글 업로드 사진
    var profileImagePath: String, // 프로필 사진
    var likeCount:Int
)

interface QnaService { // qna 글 업로드 인터페이스, qnaUpload에서 사용
    @POST("qna/create")
    fun requestQna(@Body qnaRequest: QnaRequest): Call<QnaResponse>
}

data class QnaRequest( // 게시글 작성 시 전송값, qnaUpload에서 사용
    var title: String,
    var content: String,
    var base64Image: String?
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



interface TopQnaGetService { // qna 인기 게시글 조회
    @GET("qna/popular")
    fun topQnaGetList (
    ): Call<QnaListResponse>
}




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




interface QnaHeartService { // qna 게시글 좋아요 누를 때
    @PUT("qna/like/{qnaId}")
    fun qnaHeartPut(@Path("qnaId") id:Long): Call<QnaHeart>
}

data class QnaHeart(
    val result: Boolean,
    var message: String,
    var data: Int // likeCount값
)




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

data class Comment ( // 댓글
    val commentId: Long,
    val comment: String,
    val nickname: String,
    val currentDateTime: String,
    val cocommentCount: Long,
    val role: String,
    var profileImagePath: String
    )





interface CommentDeleteService { // 댓글 삭제
    @DELETE("comment/delete/{commentId}")
    fun commentDeletePost(@Path("commentId") id: Long): Call<Void>
}