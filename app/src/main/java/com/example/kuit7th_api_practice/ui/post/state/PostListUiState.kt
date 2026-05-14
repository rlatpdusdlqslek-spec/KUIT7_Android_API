package com.example.kuit7th_api_practice.ui.post.state

import com.example.kuit7th_api_practice.domain.repository.model.Post

sealed interface PostListUiState {
    data object Loading : PostListUiState
    data class Success(val posts: List<Post>) : PostListUiState
    data class Error(val message: String) : PostListUiState
}
