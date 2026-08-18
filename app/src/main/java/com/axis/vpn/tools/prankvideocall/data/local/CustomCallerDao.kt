package com.axis.vpn.tools.prankvideocall.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCallerDao {


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insert(
        caller: CustomCallerEntity
    )


    @Query(
        "SELECT * FROM custom_callers ORDER BY id DESC"
    )
    fun getAll():
            Flow<List<CustomCallerEntity>>


    @Delete
    suspend fun delete(
        caller: CustomCallerEntity
    )

}