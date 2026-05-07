package com.example.kuit7th_api_practice.data.model.response

data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val author: AuthorResponse,
    val createdAt: String,
    val updatedAt: String,
    var isFavorite: Boolean = false
)
