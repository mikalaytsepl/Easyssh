package com.example.easyssh.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Server::class, SshKey::class, Snippet::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun sshKeyDao(): SshKeyDao
    abstract fun snippetDao(): SnippetDao
}