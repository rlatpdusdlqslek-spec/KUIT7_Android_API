package com.example.kuit7th_api_practice.ui.post.state

data class PostEditFormState(
    val title: String = "",
    val body: String = "",
    val userId: Int = 1,
    val initializedPostId: Int? = null
)
