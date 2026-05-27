package com.example.easyssh.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyssh.EasySshApplication
import com.example.easyssh.data.SshKey
import com.example.easyssh.data.SshKeyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SshKeyViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SshKeyRepository((app as EasySshApplication).database.sshKeyDao())

    val keys: StateFlow<List<SshKey>> = repo.getAllKeys()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addKey(key: SshKey) {
        viewModelScope.launch { repo.insert(key) }
    }

    fun deleteKey(key: SshKey) {
        viewModelScope.launch { repo.delete(key) }
    }
}