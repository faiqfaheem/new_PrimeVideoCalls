package com.axis.vpn.tools.prankvideocall.data.repository

import com.axis.vpn.tools.prankvideocall.data.remote.ApiService
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.CategoryResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankChatResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponseSchedule
class HomeRepository(
    private val apiService: ApiService
) {

    private var cachedVideos: List<PrankVideoResponse>? = null

    private var cachedChat: List<PrankChatResponse>? = null

    private var cachedVideosSchedule: List<PrankVideoResponseSchedule>? = null

    private var cachedSounds: List<CategoryResponse>? = null


    suspend fun getVideos(): List<PrankVideoResponse> {

        cachedVideos?.let {
            return it
        }

        val response = apiService.getPrankVideos()

        cachedVideos = response

        return response
    }


    suspend fun getChat(): List<PrankChatResponse> {

        cachedChat?.let {
            return it
        }

        val response = apiService.getChatVideos()

        cachedChat = response

        return response
    }


    suspend fun getVideosSchedule(): List<PrankVideoResponseSchedule> {

        cachedVideosSchedule?.let {
            return it
        }

        val response = apiService.getPrankVideosSchedule()

        cachedVideosSchedule = response

        return response
    }


    suspend fun getSounds(): List<CategoryResponse> {

        cachedSounds?.let {
            return it
        }

        val response = apiService.getPrankSounds()

        cachedSounds = response

        return response
    }
}