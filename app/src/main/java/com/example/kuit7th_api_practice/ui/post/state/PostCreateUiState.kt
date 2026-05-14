package com.example.kuit7th_api_practice.ui.post.state

import com.example.kuit7th_api_practice.domain.repository.model.Post

sealed interface PostCreateUiState {
    data object Idle : PostCreateUiState
    data object Saving : PostCreateUiState
    data class Success(val post: Post) : PostCreateUiState
    data class Error(val message: String) : PostCreateUiState
}
