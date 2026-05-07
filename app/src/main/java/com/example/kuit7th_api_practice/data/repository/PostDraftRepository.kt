package com.example.kuit7th_api_practice.data.repository

import com.example.kuit7th_api_practice.data.model.PostDraft

interface PostDraftRepository {
    suspend fun getDraft(): PostDraft
    suspend fun setAuthor(author: String)
    suspend fun setTitle(title: String)
    suspend fun setContent(content: String)
    suspend fun setImageUri(uri: String?)
    suspend fun clearDraft()
}
