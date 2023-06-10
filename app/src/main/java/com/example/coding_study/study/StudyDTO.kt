package com.example.coding_study.study

import retrofit2.Call
import retrofit2.http.*

//게시글 작성 시 응답 값
data class StudyResponse (
    //변수명이 JSON에 있는 키값과 같아야함
    var result: Boolean,
    var message: String,
    var data: RecruitmentDto?
    )

data class RecruitmentDto (
    var title: String = "",
    var content: String = "",
    var nickname: String = "",
    var currentDateTime : String = "",
    var recruitmentId : Long, // 게시글 번호
    var address: String ="",
    var count : Int,
    var currentCount: Int,
    var field: String,
    var profileImagePath: String
)

//input
interface StudyService { // 게시글 작성 인터페이스
    @POST("recruitments/create")
    fun requestStudy(@Body studyrequest: StudyRequest): Call<StudyResponse>
}

// 게시글 작성 요청 데이터
data class StudyRequest(
    val title: String,
    val content: String,
    val count: Long,
    var field: String
)




interface StudyGetService { // 게시글 조회 인터페이스
    @GET("recruitments/main") // 주소, 필드가 같은 게시글
    fun studyGetList(
        @Query("address") address: String? // 처음 로그인 시 null 전달을 위해 address에 null 허용
    ): Call<StudyListResponse>
}

data class StudyListResponse ( // 게시글 응답값 (스터디 게시판에서 게시글 전체 불러오기)
    //변수명이 JSON에 있는 키값과 같아야함
    var result: Boolean,
    var message: String,
    var data: List<RecruitmentDto>? // 게시글 데이터를 리스트로 받음
)





interface StudyOnlyService { // 게시글 하나만 조회 인터페이스
    @GET("recruitments/{id}")
    fun getOnlyPost(
        @Path("id") postId: Long
    ): Call<StudyOnlyResponse>
}

data class StudyOnlyResponse( // 게시글 하나만 조회할 때 응답값 (Map으로 Role 정보 받음)
    var result: Boolean,
    var message: String,
    var data: Map<Role, Any> // 서버에서 Role-게시물 정보를 Map으로 전달해줌
)

enum class Role{ //
    GUEST,
    HOST,
    PARTICIPATION
}




interface StudySearchService { // 스터디 게시판 검색 api
    @GET("recruitments/search")
    fun studySearch(
        @Query("keyword") keyword: String,
        @Query("address") address: String?
    ): Call<StudyListResponse>
}

interface StudyDeleteService { // 스터디 게시글 삭제
    @DELETE("recruitments/delete/{id}")
    fun deletePost(@Path("id") id: Long): Call<Void>
}

interface StudyEditService{ // 스터디 게시글 수정
    @PUT("recruitments/update/{id}")
    fun editPost(@Path("id") id: Long, @Body studyEdit: StudyRequest): Call<StudyResponse>
}




interface StudyParticipateService { // 참여하기 api
    @PUT("recruitments/participate/{id}")
    fun participateStudy(
        @Path("id") id:Long,
        @Query("isParticipating") isParticipating: Boolean
    ): Call<StudyGuestCurrentCount>
}

data class StudyGuestCurrentCount( // 참여하기, 취소하기 버튼 누를 때 currentCount 응답값
    var result: Boolean,
    var message: String,
    var data: Any
)

data class ChatRoom(
    var roomId: Long,
    var title: String,
    var hostNickname: String,
    var currentDateTime: String,
    var currentCount: Int
    )