package com.example.kuit7th_api_practice.ui.post.viewmodel

import android.R.attr.value
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kuit7th_api_practice.data.mock.PostLocalDataSource
import com.example.kuit7th_api_practice.data.model.request.PostCreateRequest
import com.example.kuit7th_api_practice.data.repository.FavoriteRepository
import com.example.kuit7th_api_practice.data.repository.PostDraftRepository
import com.example.kuit7th_api_practice.ui.post.state.PostCreateFormState
import com.example.kuit7th_api_practice.ui.post.state.PostCreateUiState
import com.example.kuit7th_api_practice.ui.post.state.PostDetailUiState
import com.example.kuit7th_api_practice.ui.post.state.PostEditFormState
import com.example.kuit7th_api_practice.ui.post.state.PostEditUiState
import com.example.kuit7th_api_practice.ui.post.state.PostListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.runCatching

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postLocalDataSource: PostLocalDataSource,
    private val favoriteRepository: FavoriteRepository,
    private val postDraftRepository: PostDraftRepository
) : ViewModel() {

    init {
        loadDraft()
    }

    private fun loadDraft() {
        viewModelScope.launch {
            val draft = postDraftRepository.getDraft()
            postCreateFormState = postCreateFormState.copy(
                author = draft.author,
                title = draft.title,
                content = draft.content,
                selectedImageUri = draft.imageUri
            )
        }
    }
    // TODO: 목록 화면 상태 만들기
    var postListUiState by mutableStateOf<PostListUiState>(PostListUiState.Loading)
        private set
    var postDetailUiState by mutableStateOf<PostDetailUiState>(PostDetailUiState.Loading)
        private set
    var postCreateUiState by mutableStateOf<PostCreateUiState>(PostCreateUiState.Idle)
        private set
    var postEditUiState by mutableStateOf<PostEditUiState>(PostEditUiState.Loading)
        private set

    var postCreateFormState by mutableStateOf(PostCreateFormState())
        private set
    var postEditFormState by mutableStateOf(PostEditFormState())
        private set
    var isUploading by mutableStateOf(false)
        private set

    fun getPosts() {
        viewModelScope.launch {
            postListUiState = PostListUiState.Loading
            runCatching {
                postLocalDataSource.getPosts()
            }.onSuccess { posts ->
                val map = favoriteRepository.getFavorites(posts)

                map.forEach { (key, value) ->
                    posts.find {it.id == key}?.isFavorite = value
                }
                postListUiState = PostListUiState.Success(posts)
            }.onFailure { error ->
                postListUiState = PostListUiState.Error(
                    error.message ?: "게시글 목록을 불러오지 못했습니다."
                )
            }
        }
    }

    fun getPostDetail(postId: Long) {
        viewModelScope.launch {
            postDetailUiState = PostDetailUiState.Loading
            postEditUiState = PostEditUiState.Loading

            runCatching {
                postLocalDataSource.getPostDetail(postId)
            }.onSuccess { post ->
                if (post == null) {
                    val message = "게시글을 찾을 수 없습니다."
                    postDetailUiState = PostDetailUiState.Error(message)
                    postEditUiState = PostEditUiState.Error(message)
                } else {
                    postDetailUiState = PostDetailUiState.Success(post)
                    postEditUiState = PostEditUiState.Ready(post)
                    initializeEditForm(post.id, post.title, post.content, post.imageUrl)
                }
            }.onFailure { error ->
                val message = error.message ?: "게시글을 불러오지 못했습니다."
                postDetailUiState = PostDetailUiState.Error(message)
                postEditUiState = PostEditUiState.Error(message)
            }
        }
    }

    fun onFavoriteClick(postId: Long) {
        viewModelScope.launch {
            favoriteRepository.setFavorite(postId)

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

    // TODO: createPost(), updatePost(), deletePost() 구현하기
    fun createPost(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isUploading = true
            postCreateUiState = PostCreateUiState.Saving

            runCatching {
                val author = postCreateFormState.author
                val request = PostCreateRequest(
                    title = postCreateFormState.title,
                    content = postCreateFormState.content,
                    imageUrl = postCreateFormState.selectedImageUri
                )
                postLocalDataSource.createPost(author, request)
            }.onSuccess { post ->
                postCreateUiState = PostCreateUiState.Success(post)
                postCreateFormState = PostCreateFormState()
                postDraftRepository.clearDraft()
                getPosts()
                onSuccess()
            }.onFailure { error ->
                val message = error.message ?: "게시글을 생성할 수 없습니다."
                postCreateUiState = PostCreateUiState.Error(message)
            }
            postCreateUiState = PostCreateUiState.Idle
            isUploading = false
        }
    }

    fun updatePost(postId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isUploading = true
            postEditUiState = PostEditUiState.Saving

            runCatching {
                val request = PostCreateRequest(
                    title = postEditFormState.title,
                    content = postEditFormState.content,
                    imageUrl = postEditFormState.selectedImageUri
                        ?: postEditFormState.originalImageUrl
                )
                postLocalDataSource.updatePost(postId, request)
            }.onSuccess { post ->
                if (post == null) {
                    postEditUiState = PostEditUiState.Error("게시글 수정에 실패했습니다.")
                } else {
                    postEditUiState = PostEditUiState.Success(post)
                    getPosts()
                    onSuccess()
                }
            }.onFailure { error ->
                val message = error.message ?: "게시글 수정 중 오류가 발생했습니다."
                postEditUiState = PostEditUiState.Error(message)
            }
            postEditUiState = PostEditUiState.Loading
            isUploading = false
        }
    }

    fun deletePost(postId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                postLocalDataSource.deletePost(postId)
            }.onSuccess {
                getPosts()
                onSuccess()
            }.onFailure {

            }
        }
    }

    // 게시글 생성 창에서 변수 최신화
    fun onUpdateAuthor(author: String) {
        postCreateFormState = postCreateFormState.copy(author = author)
        viewModelScope.launch {
            postDraftRepository.setAuthor(author)
        }
    }

    fun onUpdateTitle(title: String) {
        postCreateFormState = postCreateFormState.copy(title = title)
        viewModelScope.launch {
            postDraftRepository.setTitle(title)
        }
    }

    fun onUpdateContent(content: String) {
        postCreateFormState = postCreateFormState.copy(content = content)
        viewModelScope.launch {
            postDraftRepository.setContent(content)
        }
    }

    fun onUpdateSelectedImageUri(selectedImageUri: Uri?) {
        val uriString = selectedImageUri?.toString()
        postCreateFormState = postCreateFormState.copy(selectedImageUri = uriString)
        viewModelScope.launch {
            postDraftRepository.setImageUri(uriString)
        }
    }

    // 게시글 수정 창에서 변수 최신화
    fun onUpdateEditTitle(title: String) {
        postEditFormState = postEditFormState.copy(title = title)
    }

    fun onUpdateEditContent(content: String) {
        postEditFormState = postEditFormState.copy(content = content)
    }

    // TODO: 이미지 선택 상태 처리 함수 만들기
    fun onUpdateEditSelectedImageUri(selectedImageUri: Uri?) {
        postEditFormState = postEditFormState.copy(
            selectedImageUri = selectedImageUri?.toString(),
            originalImageUrl = if (selectedImageUri != null) null else postEditFormState.originalImageUrl
        )
    }

    // 게시글 수정 창에서 업로드 이미지 삭제 시
    fun onClearEditImages() {
        postEditFormState = postEditFormState.copy(
            originalImageUrl = null,
            selectedImageUri = null
        )
    }
}
