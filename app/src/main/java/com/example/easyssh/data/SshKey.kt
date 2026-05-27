package com.example.easyssh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ssh_keys")
data class SshKey(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val keyType: String, // e.g., "RSA", "ED25519"
    val privateKey: String,
    val publicKey: String? = null
)
