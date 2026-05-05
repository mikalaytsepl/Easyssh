package com.example.easyssh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class Server(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                    // this is the primary key here
    val name: String,
    val ip: String,
    val port: Int = 22,
    val username: String,
    val environment: String,
    val keyId: Int? = null
)