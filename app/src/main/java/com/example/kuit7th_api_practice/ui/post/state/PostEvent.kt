package com.example.kuit7th_api_practice.ui.post.state

sealed interface PostEvent{
    data object NavigateBack: PostEvent
    data class  ShowSnackbar(val message: String): PostEvent
}