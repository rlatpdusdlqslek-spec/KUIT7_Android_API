package com.example.kuit7th_api_practice.ui.post.state

import com.example.kuit7th_api_practice.domain.repository.model.Post

sealed interface PostEditUiState {
    data object Loading : PostEditUiState
    data class Ready(val post: Post) : PostEditUiState
    data object Saving : PostEditUiState
    data class Success(val post: Post) : PostEditUiState
    data class Error(val message: String) : PostEditUiState
}
