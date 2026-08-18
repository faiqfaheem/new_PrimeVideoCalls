package com.axis.vpn.tools.prankvideocall.data.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity
import com.axis.vpn.tools.prankvideocall.data.repository.CallerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CallerViewModel(
    private val repository: CallerRepository
) : ViewModel() {


    fun insertCaller(
        caller: CustomCallerEntity
    ) {

        viewModelScope.launch {

            repository.saveCaller(caller)

        }

    }


    val callers: Flow<List<CustomCallerEntity>> =
        repository.getCallers()

}