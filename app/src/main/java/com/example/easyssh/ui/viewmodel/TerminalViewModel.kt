package com.example.easyssh.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyssh.EasySshApplication
import com.example.easyssh.data.Server
import com.example.easyssh.ssh.SshSession
import com.example.easyssh.ssh.SshSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Cienka warstwa nad SshSessionManager — sesje żyją na poziomie aplikacji,
 * więc przeżywają nawigację między ekranami (powrót do serwera = ta sama sesja).
 */
class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as EasySshApplication).database
    private val manager: SshSessionManager = (application as EasySshApplication).sshSessionManager

    fun sessionFor(serverId: Int): SshSession = manager.getOrCreate(serverId)

    fun connect(server: Server, password: String? = null, selectedKeyId: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val sshKey = selectedKeyId?.let { db.sshKeyDao().getKeyById(it) }
            db.serverDao().setLastConnected(server.id, System.currentTimeMillis()) // sekcja "Ostatnio używane"
            sessionFor(server.id).connect(server, password = password, sshKey = sshKey)
        }
    }

    // Celowo brak rozłączania w onCleared() — sesje mają przetrwać zniszczenie ViewModelu.
}
