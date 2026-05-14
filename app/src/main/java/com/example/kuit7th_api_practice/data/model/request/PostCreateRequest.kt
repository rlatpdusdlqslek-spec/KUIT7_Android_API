package com.example.kuit7th_api_practice.data.model.request

import kotlinx.serialization.Serializable

@Serializable
data class PostCreateRequest(
    val title: String,
    val body: String,
    val userId: Int
)
