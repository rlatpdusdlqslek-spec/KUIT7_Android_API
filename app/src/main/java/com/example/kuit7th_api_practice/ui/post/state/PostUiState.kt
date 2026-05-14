package com.example.kuit7th_api_practice.ui.post.state

import com.example.kuit7th_api_practice.domain.repository.model.Post

sealed interface PostUiState{
    data object Idle: PostUiState
    data object Loading: PostUiState
    data class Success(val posts: List<Post>): PostUiState
    data class Error(val message: String): PostUiState
}
