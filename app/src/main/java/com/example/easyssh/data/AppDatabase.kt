package com.example.easyssh.data

import androidx.room.Database
import androidx.room.RoomDatabase

// connecting entity with DAO
@Database(entities = [Server::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
}