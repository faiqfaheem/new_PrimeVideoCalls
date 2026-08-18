package com.axis.vpn.tools.prankvideocall.data.repository

import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity
import com.axis.vpn.tools.prankvideocall.data.local.CustomCallerDao
import kotlinx.coroutines.flow.Flow

class CallerRepositoryImpl(
    private val dao: CustomCallerDao
) : CallerRepository {


    override suspend fun saveCaller(
        caller: CustomCallerEntity
    ) {

        dao.insert(caller)

    }


    override fun getCallers():
            Flow<List<CustomCallerEntity>> {

        return dao.getAll()

    }


    override suspend fun deleteCaller(
        caller: CustomCallerEntity
    ) {

        dao.delete(caller)

    }

}