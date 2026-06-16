package com.example.easyssh.ssh

import com.example.easyssh.data.Server
import com.example.easyssh.data.SshKey
import com.example.easyssh.security.KeyVault
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.OutputStream
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Pojedyncza sesja SSH powiązana z serwerem. Żyje niezależnie od ekranów —
 * własny scope który nie pozwala sesji przetrwać nawigację między ekranami.
 */
class SshSession(
    val serverId: Int,
    private val onStateChanged: (Int, ConnectionState) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var session: Session? = null
    private var channel: ChannelShell? = null
    private var outputStream: OutputStream? = null

    // Scrollback odtwarzany przy ponownym wejściu na ekran terminala
    private val scrollback = StringBuilder()
    private val mutex = Mutex()

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state = _state.asStateFlow()

    private val _output = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val output = _output.asSharedFlow()

    private fun setState(newState: ConnectionState) {
        _state.value = newState
        onStateChanged(serverId, newState)
    }

    fun connect(server: Server, password: String? = null, sshKey: SshKey? = null, passphrase: String? = null) {
        if (_state.value is ConnectionState.Connecting || _state.value is ConnectionState.Connected) return
        setState(ConnectionState.Connecting)

        scope.launch {
            try {
                val jsch = JSch()

                sshKey?.let { key ->
                    val rawPem = key.privateKey
                    if (KeyVault.isProtected(rawPem)) {
                        // Klucz chroniony naszym passphrase — odszyfruj do postaci jawnej
                        val plain = try {
                            KeyVault.decrypt(rawPem, passphrase ?: "")
                        } catch (e: Exception) {
                            throw RuntimeException("Błędne hasło klucza (passphrase)")
                        }
                        jsch.addIdentity(key.name, plain.toByteArray(), null, null)
                    } else {
                        // Klucz jawny lub zaszyfrowany standardowo (import). Jeśli jest zaszyfrowany,
                        // walidujemy passphrase od razu (zamiast czekać na "USERAUTH fail" z serwera).
                        val pass = passphrase?.takeIf { it.isNotEmpty() }?.toByteArray()
                        val probe = KeyPair.load(jsch, rawPem.toByteArray(), null)
                        val needsPass = probe.isEncrypted
                        val unlocked = if (needsPass) (pass != null && probe.decrypt(pass)) else true
                        probe.dispose()
                        if (needsPass && !unlocked) {
                            throw RuntimeException("Błędne hasło klucza (passphrase)")
                        }
                        jsch.addIdentity(key.name, rawPem.toByteArray(), null, pass)
                    }
                }

                val username = server.username.trim()
                val ip = server.ip.trim()

                val newSession = jsch.getSession(username, ip, server.port)
                if (!password.isNullOrEmpty()) {
                    newSession.setPassword(password)
                }

                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                newSession.setConfig(config)
                session = newSession

                emitOutput("Connecting to $ip...\r\n")
                newSession.connect(30000)

                val shellChannel = newSession.openChannel("shell") as ChannelShell
                channel = shellChannel

                val inputStream = shellChannel.inputStream
                outputStream = shellChannel.outputStream

                shellChannel.setPtyType("xterm")
                shellChannel.connect()

                setState(ConnectionState.Connected)
                emitOutput("Connected.\r\n")

                // Pętla czytająca — działa aż do EOF/rozłączenia
                val buffer = ByteArray(2048)
                var i: Int
                try {
                    while (inputStream.read(buffer).also { i = it } != -1) {
                        emitOutput(String(buffer, 0, i))
                    }
                } catch (e: Exception) {
                    // strumień zamknięty (rozłączenie lub błąd sieci)
                }
                emitOutput("\r\n[Session Closed]\r\n")
                cleanup()
                setState(ConnectionState.Disconnected)
            } catch (e: Exception) {
                emitOutput("\r\nError: ${e.message}\r\n")
                cleanup()
                setState(ConnectionState.Error(e.message ?: "Połączenie nieudane"))
            }
        }
    }

    fun sendData(data: String) {
        if (_state.value !is ConnectionState.Connected) return
        scope.launch {
            try {
                outputStream?.write(data.toByteArray())
                outputStream?.flush()
            } catch (e: Exception) {
                emitOutput("\r\n[Failed to send data]\r\n")
            }
        }
    }

    fun sendCommand(command: String) = sendData(command + "\n")

    fun disconnect() {
        scope.launch {
            cleanup()
            setState(ConnectionState.Disconnected)
            emitOutput("\r\n[Disconnected]\r\n")
        }
    }

    /** Kopia całego dotychczasowego outputu — do odtworzenia w xterm po powrocie na ekran. */
    suspend fun snapshot(): String = mutex.withLock { scrollback.toString() }

    private suspend fun emitOutput(text: String) {
        mutex.withLock {
            scrollback.append(text)
            if (scrollback.length > SCROLLBACK_LIMIT) {
                scrollback.delete(0, scrollback.length - SCROLLBACK_LIMIT)
            }
        }
        _output.emit(text)
    }

    private fun cleanup() {
        runCatching { channel?.disconnect() }
        runCatching { session?.disconnect() }
        channel = null
        session = null
        outputStream = null
    }

    companion object {
        private const val SCROLLBACK_LIMIT = 100_000
    }
}

/**
 * Menedżer sesji na poziomie aplikacji — sesje przeżywają nawigację
 * i niszczenie ViewModeli. Jedna sesja per serwer, wiele równoległych.
 */
class SshSessionManager {
    private val sessions = ConcurrentHashMap<Int, SshSession>()

    private val _activeSessions = MutableStateFlow<Set<Int>>(emptySet())
    val activeSessions = _activeSessions.asStateFlow()

    fun getOrCreate(serverId: Int): SshSession =
        sessions.getOrPut(serverId) {
            SshSession(serverId, onStateChanged = ::updateActive)
        }

    private fun updateActive(serverId: Int, state: ConnectionState) {
        _activeSessions.value =
            if (state is ConnectionState.Connected) _activeSessions.value + serverId
            else _activeSessions.value - serverId
    }
}
