package com.axis.vpn.tools.prankvideocall.data.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axis.vpn.tools.prankvideocall.data.entity.CustomCallerEntity
import com.axis.vpn.tools.prankvideocall.data.repository.CallerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
class StartViewModel(
    private val repository: CallerRepository
) : ViewModel() {

    val callers: Flow<List<CustomCallerEntity>> =
        repository.getCallers()

    fun deleteCaller(
        caller: CustomCallerEntity
    ) {
        viewModelScope.launch {
            repository.deleteCaller(caller)
        }
    }
}