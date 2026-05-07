package com.example.kuit7th_api_practice.data.repository

import com.example.kuit7th_api_practice.data.model.response.PostResponse

interface FavoriteRepository {
    suspend fun getFavorites(posts: List<PostResponse>): Map<Long, Boolean>
    suspend fun getFavorite(id: Long): Boolean
    suspend fun setFavorite(id: Long)
}