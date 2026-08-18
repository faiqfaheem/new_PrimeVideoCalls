package com.axis.vpn.tools.prankvideocall.data.remote

import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.CategoryResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankChatResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponseSchedule
import retrofit2.http.GET

interface ApiService {

    @GET("api/prank-sound/")
    suspend fun getPrankSounds(): List<CategoryResponse>

    @GET("api/prank-video/")
    suspend fun getPrankVideos(): List<PrankVideoResponse>

    @GET("api/prank-video2/")
    suspend fun getPrankVideosSchedule(): List<PrankVideoResponseSchedule>

    @GET("api/prank-chat/")
    suspend fun getChatVideos(): List<PrankChatResponse>
}