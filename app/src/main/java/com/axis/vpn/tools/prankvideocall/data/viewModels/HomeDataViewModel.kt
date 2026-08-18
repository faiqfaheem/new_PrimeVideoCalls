package com.axis.vpn.tools.prankvideocall.data.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axis.vpn.tools.prankvideocall.data.repository.HomeRepository
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.CategoryResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankChatResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponse
import com.axis.vpn.tools.prankvideocall.ui.fragments.home.model.PrankVideoResponseSchedule
import com.axis.vpn.tools.prankvideocall.utils.constants.VideoCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeDataViewModel(
    private val repository: HomeRepository
) : ViewModel() {

    private val _videos =
        MutableLiveData<List<PrankVideoResponse>>()

    val videos: LiveData<List<PrankVideoResponse>>
        get() = _videos


    private val _chat =
        MutableLiveData<List<PrankChatResponse>>()

    val chat: LiveData<List<PrankChatResponse>>
        get() = _chat


    private val _scheduleVideos =
        MutableLiveData<List<PrankVideoResponseSchedule>>()

    val scheduleVideos:
            LiveData<List<PrankVideoResponseSchedule>>
        get() = _scheduleVideos


    private val _categories =
        MutableLiveData<List<CategoryResponse>>()

    val categories:
            LiveData<List<CategoryResponse>>
        get() = _categories


    private val _loading =
        MutableLiveData(false)

    val loading:
            LiveData<Boolean>
        get() = _loading


    private var dataLoaded = false


    fun preloadData(
        context: Context
    ) {

        if (dataLoaded) {
            return
        }


        if (_loading.value == true) {
            return
        }


        viewModelScope.launch {

            _loading.value = true

            try {

                val videos =
                    repository.getVideos()

                val chat =
                    repository.getChat()

                val scheduleVideos =
                    repository.getVideosSchedule()

                val categories =
                    repository.getSounds()


                _videos.value =
                    videos

                _chat.value =
                    chat

                _scheduleVideos.value =
                    scheduleVideos

                _categories.value =
                    categories


                dataLoaded = true


                /*
                 * Start video caching AFTER data
                 * has been loaded.
                 *
                 * This does NOT block the UI.
                 */
                preloadVideos(
                    context.applicationContext,
                    videos
                )


            } catch (e: Exception) {

                Log.e(
                    "HomeDataViewModel",
                    "Failed to load home data",
                    e
                )

            } finally {

                _loading.value = false
            }
        }
    }


    private fun preloadVideos(
        context: Context,
        videos: List<PrankVideoResponse>
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            for (video in videos) {

                try {

                    VideoCacheManager
                        .ensureVideoCached(
                            context,
                            video.videoUrl
                        )

                    Log.d(
                        "HomeDataViewModel",
                        "Cached: ${video.name}"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "HomeDataViewModel",
                        "Failed caching ${video.name}",
                        e
                    )
                }
            }
        }
    }
}
