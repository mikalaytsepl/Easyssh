package com.example.easyssh.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyssh.EasySshApplication
import com.example.easyssh.data.Snippet
import com.example.easyssh.data.SnippetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SnippetViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SnippetRepository((app as EasySshApplication).database.snippetDao())

    val snippets: StateFlow<List<Snippet>> = repo.getAllSnippets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addSnippet(snippet: Snippet) {
        viewModelScope.launch { repo.insert(snippet) }
    }

    fun deleteSnippet(snippet: Snippet) {
        viewModelScope.launch { repo.delete(snippet) }
    }
}