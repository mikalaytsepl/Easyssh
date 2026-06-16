package com.example.easyssh

import android.app.Application
import androidx.room.Room
import com.example.easyssh.data.AppDatabase
import com.example.easyssh.data.Server
import com.example.easyssh.data.Snippet
import com.example.easyssh.ssh.SshSessionManager
import com.example.easyssh.util.SoundFx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EasySshApplication : Application() {
    val sshSessionManager = SshSessionManager()

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "easyssh_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        SoundFx.init(this) // załaduj efekty dźwiękowe z res/raw (SoundPool)
        seedIfEmpty()
    }


    private fun seedIfEmpty() {
        appScope.launch {
            val serverDao = database.serverDao()
            if (serverDao.count() > 0) return@launch

            // Serwery: różne dystrybucje (ikony) i środowiska (filtry/badge)
            listOf(
                Server(name = "ProdWeb-01",  ip = "192.168.1.10", port = 22,   username = "root",     environment = "PROD", distro = "ubuntu"),
                Server(name = "ProdDB-02",   ip = "192.168.1.11", port = 22,   username = "postgres", environment = "PROD", distro = "debian"),
                Server(name = "QA-Runner",   ip = "10.0.5.20",    port = 22,   username = "ci",       environment = "QA",   distro = "centos"),
                Server(name = "DevBox",      ip = "10.0.5.50",    port = 2222, username = "dev",      environment = "DEV",  distro = "fedora"),
                Server(name = "Backup-Node", ip = "192.168.1.40", port = 22,   username = "admin",    environment = "PROD", distro = "rocky"),
                Server(name = "Monitoring",  ip = "10.0.5.99",    port = 22,   username = "ops",      environment = "DEV",  distro = "linux"),
            ).forEach { serverDao.insertServer(it) }

            // Snippety: przykładowe komendy w kilku kategoriach
            val snippetDao = database.snippetDao()
            listOf(
                Snippet(title = "Restart Nginx",          category = "System", command = "sudo systemctl restart nginx"),
                Snippet(title = "Zużycie dysku",          category = "System", command = "df -h"),
                Snippet(title = "Logi systemowe",         category = "System", command = "journalctl -xe --no-pager | tail -n 50"),
                Snippet(title = "Aktualizacja pakietów",  category = "System", command = "sudo apt update && sudo apt upgrade -y"),
                Snippet(title = "Lista kontenerów",       category = "Docker", command = "docker ps -a"),
                Snippet(title = "Restart kontenera",      category = "Docker", command = "docker restart nazwa_kontenera"),
                Snippet(title = "Otwarte porty",          category = "Sieć",   command = "ss -tulpn"),
            ).forEach { snippetDao.insertSnippet(it) }
        }
    }
}
