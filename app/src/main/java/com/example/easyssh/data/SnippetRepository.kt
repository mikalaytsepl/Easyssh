package com.example.easyssh.data

import kotlinx.coroutines.flow.Flow

class SnippetRepository(private val dao: SnippetDao) {
    fun getAllSnippets(): Flow<List<Snippet>> = dao.getAllSnippets()
    fun getSnippetsByCategory(category: String): Flow<List<Snippet>> = dao.getSnippetsByCategory(category)
    suspend fun insert(snippet: Snippet) = dao.insertSnippet(snippet)
    suspend fun delete(snippet: Snippet) = dao.deleteSnippet(snippet)
}