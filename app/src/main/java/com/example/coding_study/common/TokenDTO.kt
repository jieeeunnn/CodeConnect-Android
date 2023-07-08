package com.example.coding_study.common

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenService {
    @POST("refresh-token")
    fun requestToken(@Body tokenRequest: Token): Call<TokenResponse>
}

data class TokenResponse (
    var result: Boolean,
    var message: String,
    var data: String?
)



