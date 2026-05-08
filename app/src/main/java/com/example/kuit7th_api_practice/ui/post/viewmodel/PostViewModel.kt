package com.example.kuit7th_api_practice.ui.post.viewmodel

import android.R.id.message
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kuit7th_api_practice.data.mock.PostLocalDataSource
import com.example.kuit7th_api_practice.ui.post.state.PostCreateFormState
import com.example.kuit7th_api_practice.ui.post.state.PostCreateUiState
import com.example.kuit7th_api_practice.ui.post.state.PostDetailUiState
import com.example.kuit7th_api_practice.ui.post.state.PostEditFormState
import com.example.kuit7th_api_practice.ui.post.state.PostEditUiState
import com.example.kuit7th_api_practice.ui.post.state.PostListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.kuit7th_api_practice.data.model.request.PostCreateRequest
import com.example.kuit7th_api_practice.data.repository.DraftRepository
import com.example.kuit7th_api_practice.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.first

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postLocalDataSource: PostLocalDataSource,
    private val favoriteRepository: FavoriteRepository,
    private val draftRepository: DraftRepository
) : ViewModel() {

    var postListUiState by mutableStateOf<PostListUiState>(value = PostListUiState.Loading)
        private set
    var postDetailUiState by mutableStateOf<PostDetailUiState>(value = PostDetailUiState.Loading)
        private set

    var postEditUistate by mutableStateOf<PostEditUiState>(value = PostEditUiState.Loading)
        private set
    var postCreateUiState by mutableStateOf(value = PostCreateFormState())
        private set
    var postEditFormState by mutableStateOf(value = PostEditFormState())
        private set
    var isUploadingImage by mutableStateOf(value = false)
        private set

    init {
        loadDraft()
    }

    private fun loadDraft() {
        viewModelScope.launch {
            val author = draftRepository.getAuthor().first()
            val title = draftRepository.getTitle().first()
            val content = draftRepository.getContent().first()
            postCreateUiState = postCreateUiState.copy(
                author = author,
                title = title,
                content = content
            )
        }
    }

    fun getPosts() {
        viewModelScope.launch {
            postListUiState = PostListUiState.Loading
            runCatching {
                postLocalDataSource.getPosts()
            }.onSuccess { posts ->
                val map = favoriteRepository.getFavoritePosts(posts)

                map.forEach { key, value ->
                    posts.find { it.id == key }?.isFavorite = value
                }
                postListUiState = PostListUiState.Success(posts)

            }.onFailure { error ->
                postListUiState = PostListUiState.Error(error.message ?: "Unknown Error")

            }

        }
    }

    fun getPostDetail(postId: Long) {
        viewModelScope.launch {
            postDetailUiState = PostDetailUiState.Loading
            postEditUistate = PostEditUiState.Loading

            runCatching {
                postLocalDataSource.getPostDetail(postId)
            }.onSuccess { post ->
                if (post == null) {
                    val message = "게시글을 찾을 수 없습니다."
                    postDetailUiState = PostDetailUiState.Error(message)
                    postEditUistate = PostEditUiState.Error(message)
                } else {
                    postDetailUiState = PostDetailUiState.Success(post)
                    postEditUistate = PostEditUiState.Ready(post)
                    initializeEditForm(
                        postId = post.id, post.title, post.content, post.imageUrl
                    )
                }
            }.onFailure { error ->
                val message = error.message ?: "게시글을 불러오지 못했습니다."
                postDetailUiState = PostDetailUiState.Error(message)
                postEditUistate = PostEditUiState.Error(message)
            }


        }
    }

    fun onFavoriteClick(postId: Long) {
        viewModelScope.launch {
            favoriteRepository.addFavoritePost(postId)


            getPosts()
        }
    }

    private fun initializeEditForm(
        postId: Long,
        title: String,
        content: String,
        imageUrl: String?,
        force: Boolean = false

    ) {
        if (!force && postEditFormState.initializedPostId == postId) return
        postEditFormState = PostEditFormState(
            title = title,
            content = content,
            originalImageUrl = imageUrl,
            selectedImageUri = null,
            initializedPostId = postId

        )
    }


    fun updateCreateFormState(
        author: String = postCreateUiState.author,
        title: String = postCreateUiState.title,
        content: String = postCreateUiState.content
    ) {
        postCreateUiState =
            postCreateUiState.copy(author = author, title = title, content = content)

        viewModelScope.launch {
            draftRepository.saveAuthor(postCreateUiState.author)
            draftRepository.saveTitle(postCreateUiState.title)
            draftRepository.saveContent(postCreateUiState.content)
        }
    }

    fun updateEditFormState(
        title: String = postEditFormState.title,
        content: String = postEditFormState.content
    ) {
        postEditFormState = postEditFormState.copy(title = title, content = content)
    }

    fun updateCreateImage(uri: String?) {
        postCreateUiState = postCreateUiState.copy(selectedImageUri = uri)
    }

    fun updateEditImage(uri: String?) {
        postEditFormState = postEditFormState.copy(selectedImageUri = uri)
    }

    fun removeEditOriginalImage() {
        postEditFormState = postEditFormState.copy(originalImageUrl = null)
    }

    fun createPost(onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                val request = PostCreateRequest(
                    title = postCreateUiState.title,
                    content = postCreateUiState.content,
                    imageUrl = postCreateUiState.selectedImageUri
                )
                postLocalDataSource.createPost(
                    authorName = postCreateUiState.author.ifBlank { "anonymous" },
                    request = request
                )
            }.onSuccess {
                postCreateUiState = PostCreateFormState() // 초기화
                viewModelScope.launch {
                    draftRepository.clearDraft()
                }
                onSuccess()
            }
        }
    }

    fun updatePost(postId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                val request = PostCreateRequest(
                    title = postEditFormState.title,
                    content = postEditFormState.content,
                    imageUrl = postEditFormState.selectedImageUri
                        ?: postEditFormState.originalImageUrl
                )
                postLocalDataSource.updatePost(postId, request)
            }.onSuccess {
                onSuccess()
            }
        }
    }

    fun deletePost(postId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                postLocalDataSource.deletePost(postId)
            }.onSuccess {
                onSuccess()
            }
        }
    }
}
