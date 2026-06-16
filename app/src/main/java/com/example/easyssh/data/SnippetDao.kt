package com.example.easyssh.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// dao for working with snippets database
@Dao
interface SnippetDao {
    @Query("SELECT * FROM snippets")
    fun getAllSnippets(): Flow<List<Snippet>>

    @Query("SELECT * FROM snippets WHERE category = :category")
    fun getSnippetsByCategory(category: String): Flow<List<Snippet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: Snippet)

    @Delete
    suspend fun deleteSnippet(snippet: Snippet)
}