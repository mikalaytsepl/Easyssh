package com.example.easyssh.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyssh.EasySshApplication
import com.example.easyssh.data.Server
import com.example.easyssh.security.CryptoManager
import com.example.easyssh.ssh.SshSession
import com.example.easyssh.ssh.SshSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as EasySshApplication).database
    private val manager: SshSessionManager = (application as EasySshApplication).sshSessionManager

    fun sessionFor(serverId: Int): SshSession = manager.getOrCreate(serverId)

    fun connect(server: Server, password: String? = null, selectedKeyId: Int? = null, keyPassphrase: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            // Odszyfrowanie klucza prywatnego dopiero w momencie użycia
            val sshKey = selectedKeyId?.let { db.sshKeyDao().getKeyById(it) }
                ?.let { it.copy(privateKey = CryptoManager.decrypt(it.privateKey)) }
            db.serverDao().setLastConnected(server.id, System.currentTimeMillis()) // sekcja "Ostatnio używane"
            sessionFor(server.id).connect(server, password = password, sshKey = sshKey, passphrase = keyPassphrase)
        }
    }

    // Celowo brak rozłączania w onCleared() — sesje mają przetrwać zniszczenie ViewModelu.
}
