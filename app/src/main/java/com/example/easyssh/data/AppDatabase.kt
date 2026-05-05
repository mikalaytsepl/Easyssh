package com.example.easyssh.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Server::class, SshKey::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun sshKeyDao(): SshKeyDao
}
