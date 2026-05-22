package com.example.kuit7th_api_practice.data.dto

import com.example.kuit7th_api_practice.domain.repository.model.Post
import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val userId: Int,
    val id: Int? = null,
    val title: String,
    val body: String
)

fun PostDto.toDomain(fallId: Int = -1): Post = Post(
    id = id?: fallId,
    userId = userId,
    title = title,
    body = body
)