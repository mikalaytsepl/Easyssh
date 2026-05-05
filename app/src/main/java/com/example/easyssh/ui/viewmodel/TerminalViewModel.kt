package com.example.easyssh.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyssh.EasySshApplication
import com.example.easyssh.data.Server
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.Properties

class TerminalViewModel(application: Application) : AndroidViewModel(application) {
    private var session: Session? = null
    private var channel: ChannelShell? = null
    private var outputStream: OutputStream? = null

    private val _terminalOutput = MutableSharedFlow<String>()
    val terminalOutput = _terminalOutput.asSharedFlow()

    private val db = (application as EasySshApplication).database

    fun connect(server: Server, password: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsch = JSch()

                // 1. Load private key if assigned
                server.keyId?.let { keyId ->
                    val sshKey = db.sshKeyDao().getKeyById(keyId)
                    if (sshKey != null) {
                        jsch.addIdentity(sshKey.name, sshKey.privateKey.toByteArray(), null, null)
                    }
                }

                // 2. Setup Session - Trim IP and Username to avoid UnknownHostException
                val username = server.username.trim()
                val ip = server.ip.trim()
                
                session = jsch.getSession(username, ip, server.port)
                if (!password.isNullOrEmpty()) {
                    session?.setPassword(password)
                }

                val config = Properties()
                config["StrictHostKeyChecking"] = "no"
                session?.setConfig(config)

                _terminalOutput.emit("Connecting to $ip...\r\n")
                session?.connect(30000)

                // 3. Open Shell Channel
                val shellChannel = session?.openChannel("shell") as ChannelShell
                channel = shellChannel

                val inputStream = shellChannel.inputStream
                outputStream = shellChannel.outputStream

                shellChannel.setPtyType("xterm")
                shellChannel.connect()

                _terminalOutput.emit("Connected.\r\n")

                // 4. Start reading the remote output
                readStream(inputStream)
            } catch (e: Exception) {
                _terminalOutput.emit("\r\nError: ${e.message}\r\n")
            }
        }
    }

    private suspend fun readStream(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            val buffer = ByteArray(2048)
            var i: Int
            try {
                while (inputStream.read(buffer).also { i = it } != -1) {
                    _terminalOutput.emit(String(buffer, 0, i))
                }
            } catch (e: Exception) {
                _terminalOutput.emit("\r\n[Session Closed]\r\n")
            }
        }
    }

    fun sendData(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                outputStream?.write(data.toByteArray())
                outputStream?.flush()
            } catch (e: Exception) {
                _terminalOutput.emit("\r\n[Failed to send data]\r\n")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            channel?.disconnect()
            session?.disconnect()
        }
    }
}