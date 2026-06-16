package com.example.easyssh.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippets")
data class Snippet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val command: String
)