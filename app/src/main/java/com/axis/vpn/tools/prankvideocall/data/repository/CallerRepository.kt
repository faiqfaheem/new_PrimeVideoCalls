package com.axis.vpn.tools.prankvideocall.data.repository

import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity
import kotlinx.coroutines.flow.Flow

interface CallerRepository {


    suspend fun saveCaller(
        caller: CustomCallerEntity
    )


    fun getCallers():
            Flow<List<CustomCallerEntity>>


    suspend fun deleteCaller(
        caller: CustomCallerEntity
    )

}