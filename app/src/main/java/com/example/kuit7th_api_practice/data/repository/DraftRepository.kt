package com.example.kuit7th_api_practice.data.repository

import kotlinx.coroutines.flow.Flow

interface DraftRepository {
    fun getAuthor(): Flow<String>
    fun getTitle(): Flow<String>
    fun getContent(): Flow<String>

    suspend fun saveAuthor(author: String)
    suspend fun saveTitle(title: String)
    suspend fun saveContent(content: String)

    suspend fun clearDraft()
}
