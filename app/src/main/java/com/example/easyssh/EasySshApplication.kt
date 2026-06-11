package com.example.easyssh

import android.app.Application
import androidx.room.Room
import com.example.easyssh.data.AppDatabase
import com.example.easyssh.ssh.SshSessionManager

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
}
