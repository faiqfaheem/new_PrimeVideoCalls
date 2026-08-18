package com.axis.vpn.tools.prankvideocall.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GroqApi {
    @Headers("Content-Type: application/json")
    @POST("openai/v1/chat/completions")
    suspend fun generateText(
        @Body request: GroqRequest
    ): Response<GroqResponse>


}