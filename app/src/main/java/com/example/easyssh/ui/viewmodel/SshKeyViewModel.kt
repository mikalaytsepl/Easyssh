package com.example.easyssh.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyssh.EasySshApplication
import com.example.easyssh.data.SshKey
import com.example.easyssh.data.SshKeyRepository
import com.example.easyssh.security.CryptoManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SshKeyViewModel(app: Application) : AndroidViewModel(app) {
    private val db = (app as EasySshApplication).database
    private val keyDao = db.sshKeyDao()
    private val serverDao = db.serverDao()
    private val repo = SshKeyRepository(keyDao)

    val keys: StateFlow<List<SshKey>> = repo.getAllKeys()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Dodaje klucz. Jeśli klucz wskazuje serwer (serverId != null), ustawia go także jako domyślny
     * klucz tego serwera (Server.keyId) — relacja dwukierunkowa zgodnie z modelem bazy.
     */
    fun addKey(key: SshKey) {
        viewModelScope.launch {
            // Szyfrowanie klucza prywatnego w spoczynku (Android Keystore); klucz publiczny jawnie
            val toStore = key.copy(privateKey = CryptoManager.encrypt(key.privateKey))
            val newId = keyDao.insertKey(toStore).toInt()
            key.serverId?.let { sid -> serverDao.setDefaultKey(sid, newId) }
        }
    }

    fun deleteKey(key: SshKey) {
        viewModelScope.launch {
            serverDao.clearDefaultKeyEverywhere(key.id) // zerwij back-referencję, by nie wisiała martwa
            repo.delete(key)
        }
    }

    /**
     * Przypisuje/odpina istniejący klucz do serwera, utrzymując dwukierunkowość (pkt 4).
     * newServerId == null → klucz staje się ogólny (nieprzypisany).
     */
    fun assignKeyToServer(key: SshKey, newServerId: Int?) {
        viewModelScope.launch {
            keyDao.setKeyServer(key.id, newServerId)        // strona klucz → serwer
            serverDao.clearDefaultKeyEverywhere(key.id)     // usuń ten klucz jako domyślny gdziekolwiek
            if (newServerId != null) {
                serverDao.setDefaultKey(newServerId, key.id) // strona serwer → klucz
            }
        }
    }
}
