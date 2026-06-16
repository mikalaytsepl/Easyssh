package com.example.easyssh.ssh

import com.example.easyssh.data.Server
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Properties

// Instalacja klucza publicznego na serwerze (odpowiednik `ssh-copy-id`):

object KeyInstaller {

    suspend fun installPublicKey(server: Server, publicKey: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val jsch = JSch()
                val session = jsch.getSession(server.username.trim(), server.ip.trim(), server.port)
                session.setPassword(password)
                session.setConfig(Properties().apply { put("StrictHostKeyChecking", "no") })
                session.connect(30_000)

                try {
                    // klucz wstawiamy w pojedynczych cudzysłowach; ewentualne ' escapujemy
                    val safePub = publicKey.trim().replace("'", "'\\''")
                    val cmd = "mkdir -p ~/.ssh && chmod 700 ~/.ssh && " +
                        "echo '$safePub' >> ~/.ssh/authorized_keys && " +
                        "chmod 600 ~/.ssh/authorized_keys"

                    val channel = session.openChannel("exec") as ChannelExec
                    channel.setCommand(cmd)
                    val errOut = ByteArrayOutputStream()
                    channel.setErrStream(errOut)
                    channel.connect()

                    while (!channel.isClosed) Thread.sleep(100)
                    val status = channel.exitStatus
                    channel.disconnect()

                    if (status != 0) {
                        val err = errOut.toString("UTF-8").trim()
                        throw RuntimeException(if (err.isNotEmpty()) err else "Serwer zwrócił kod $status")
                    }
                } finally {
                    session.disconnect()
                }
            }
        }
}
