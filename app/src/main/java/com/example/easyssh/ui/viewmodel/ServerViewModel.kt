package com.example.easyssh.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyssh.EasySshApplication
import com.example.easyssh.data.Server
import com.example.easyssh.data.ServerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ServerRepository(
        (app as EasySshApplication).database.serverDao()
    )

    val servers: StateFlow<List<Server>> = repo.getAllServers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addServer(server: Server) {
        viewModelScope.launch { repo.insert(server) }
    }

    fun deleteServer(server: Server) {
        viewModelScope.launch { repo.delete(server) }
    }
}
