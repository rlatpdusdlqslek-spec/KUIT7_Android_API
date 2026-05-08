package com.example.kuit7th_api_practice.data.repository

import com.example.kuit7th_api_practice.data.model.response.PostResponse

interface FavoriteRepository {
    suspend fun getFavoritePosts(posts:List<PostResponse>): Map<Long, Boolean>
    suspend fun getFavoritePosts(id:Long): Boolean
    suspend fun addFavoritePost(postId: Long)
}