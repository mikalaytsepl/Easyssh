package com.example.easyssh.data

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

class SshSessionManager {
    private val sessions = mutableMapOf<Int, ActiveSession>()
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    data class ActiveSession(
        val session: Session,
        val channel: ChannelShell,
        val outputStream: OutputStream,
        val outputFlow: MutableSharedFlow<String>,
        val terminalBuffer: StringBuilder = StringBuilder()
    )

    // gets session by server id or return null if no session found
    fun getSession(serverId: Int): ActiveSession? = sessions[serverId]

    fun addSession(serverId: Int, session: Session, channel: ChannelShell, outputStream: OutputStream): ActiveSession {
        // Disconnect existing if any for this server
        sessions[serverId]?.let {
            it.channel.disconnect()
            it.session.disconnect()
        }
        
        val flow = MutableSharedFlow<String>(replay = 0)
        val activeSession = ActiveSession(session, channel, outputStream, flow)
        sessions[serverId] = activeSession
        
        startReading(serverId, channel.inputStream, activeSession)
        
        return activeSession
    }

    private fun startReading(serverId: Int, inputStream: InputStream, activeSession: ActiveSession) {
        scope.launch {
            val buffer = ByteArray(2048)
            var i: Int
            try {
                while (inputStream.read(buffer).also { i = it } != -1) {
                    val text = String(buffer, 0, i)
                    activeSession.terminalBuffer.append(text)
                    // Keep buffer size reasonable (e.g., last 50k chars)
                    if (activeSession.terminalBuffer.length > 50000) {
                        activeSession.terminalBuffer.delete(0, 10000)
                    }
                    activeSession.outputFlow.emit(text)
                }
            } catch (e: Exception) {
                activeSession.outputFlow.emit("\r\n[Session Closed]\r\n")
            } finally {
                sessions.remove(serverId)
            }
        }
    }

    fun closeAll() {
        sessions.values.forEach {
            it.channel.disconnect()
            it.session.disconnect()
        }
        sessions.clear()
    }
}
