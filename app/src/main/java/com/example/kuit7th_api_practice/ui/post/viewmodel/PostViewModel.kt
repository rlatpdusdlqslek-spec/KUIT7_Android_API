package com.example.kuit7th_api_practice.ui.post.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kuit7th_api_practice.domain.repository.PostRepository
import com.example.kuit7th_api_practice.domain.repository.model.Post
import com.example.kuit7th_api_practice.ui.post.state.PostCreateFormState
import com.example.kuit7th_api_practice.ui.post.state.PostCreateUiState
import com.example.kuit7th_api_practice.ui.post.state.PostDetailUiState
import com.example.kuit7th_api_practice.ui.post.state.PostEditFormState
import com.example.kuit7th_api_practice.ui.post.state.PostEditUiState
import com.example.kuit7th_api_practice.ui.post.state.PostEvent
import com.example.kuit7th_api_practice.ui.post.state.PostUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    //TODO 8주차 실습: UI는 읽기만 가능하고 ViewModel만 상태를 변경할 수 있도록 구조 바꾸기
    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    // TODO 8주차 미션: 상세 화면 상태도 같은 방식으로 화면 상태 스트림으로 관리하기
    private val _postDetailUiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    val postDetailUiState: StateFlow<PostDetailUiState> = _postDetailUiState.asStateFlow()

    // TODO 8주차 실습, 미션: 작성/수정 성공 후 화면 이동 같은 1회성 동작은 상태가 아니라 이벤트로 분리하기
    private val _postCreateUiState = MutableStateFlow<PostCreateUiState>(PostCreateUiState.Idle)
    val postCreateUiState: StateFlow<PostCreateUiState> = _postCreateUiState.asStateFlow()

    private val _postEditUiState = MutableStateFlow<PostEditUiState>(PostEditUiState.Loading)
    val postEditUiState: StateFlow<PostEditUiState> = _postEditUiState.asStateFlow()

    private val _postCreateFormState = MutableStateFlow(PostCreateFormState())
    var postCreateFormState: StateFlow<PostCreateFormState> = _postCreateFormState.asStateFlow()

    private val _postEditFormState = MutableStateFlow(PostEditFormState())
    var postEditFormState: StateFlow<PostEditFormState> = _postEditFormState.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    var isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PostEvent>()
    val eventFlow: SharedFlow<PostEvent> = _eventFlow.asSharedFlow()

    private val createdPosts = mutableMapOf<Int, Post>()
    private val updatedPosts = mutableMapOf<Int, Post>()
    private val deletedPostIds = mutableSetOf<Int>()

    fun fetchPosts(userId: Int? = null) {
        // TODO 8주차 실습: Loading 상태를 UI 상태 스트림에 반영하기
        _uiState.value = PostUiState.Loading

        viewModelScope.launch {
            // TODO 8주차 실습: Repository의 Flow를 collect해서 Success 상태로 변환하기
            // TODO 8주차 실습: catch 또는 runCatching 위치를 비교하며 에러 처리 흐름 이해하기
            postRepository.getPosts(userId)
                .map { posts -> applyLocalChanges(posts, userId) }
                .catch { error ->
                    _uiState.value = PostUiState.Error(error.message ?: "Unknown error")
                }
                .collect { posts ->
                    _uiState.value = PostUiState.Success(posts)
                }
        }
    }

    fun getPostDetail(postId: Long) {
        // TODO 8주차 미션: 상세 상태도 Loading, Success, Error 흐름으로 관리하기
        _postDetailUiState.value = PostDetailUiState.Loading
        _postEditUiState.value = PostEditUiState.Loading

        val localPost = findLocalPost(postId.toInt())
        if (localPost != null) {
            showPostDetail(localPost)
            return
        }

        if (deletedPostIds.contains(postId.toInt())) {
            val message = "Deleted post."
            _postDetailUiState.value = PostDetailUiState.Error(message)
            _postEditUiState.value = PostEditUiState.Error(message)
            return
        }

        viewModelScope.launch {
            runCatching {
                postRepository.getPost(postId.toInt()).first()
            }.onSuccess { post ->
                showPostDetail(post)
            }.onFailure { error ->
                val message = error.message ?: "Failed to load post."
                _postDetailUiState.value = PostDetailUiState.Error(message)
                _postEditUiState.value = PostEditUiState.Error(message)
            }
        }
    }

    fun createPost() {
        viewModelScope.launch {
            _isUploading.value = true
            _postCreateUiState.value = PostCreateUiState.Saving

            // TODO 8주차 실습: onSuccess 콜백 대신 저장 완료 이벤트를 방출하도록 바꾸기
            // TODO 8주차 실습: Toast, Snackbar, Navigation처럼 한 번만 처리할 동작을 이벤트로 분리하기
            runCatching {
                val formState = _postCreateFormState.value
                postRepository.createPost(
                    title = formState.title,
                    body = formState.content,
                    userId = formState.author.toIntOrNull() ?: 1
                )
            }.onSuccess { post ->
                createdPosts[post.id] = post
                _postCreateUiState.value = PostCreateUiState.Success(post)
                _postCreateFormState.value = PostCreateFormState()
                addOrReplacePostInList(post)
                _eventFlow.emit(PostEvent.NavigateBack)
            }.onFailure { error ->
                val message = error.message ?: "Failed to create post."
                _postCreateUiState.value = PostCreateUiState.Error(
                    message
                )
            }

            _isUploading.value = false
        }
    }

    fun updatePost(postId: Long) {
        viewModelScope.launch {
            _isUploading.value = true
            _postEditUiState.value = PostEditUiState.Saving

            // TODO 8주차 미션: 수정 성공 후 뒤로가기 처리를 1회성 이벤트로 분리하기
            runCatching {
                val formState = _postEditFormState.value
                postRepository.updatePost(
                    id = postId.toInt(),
                    title = formState.title,
                    body = formState.body,
                    userId = formState.userId
                )
            }.onSuccess { post ->
                updatedPosts[post.id] = post
                _postEditUiState.value = PostEditUiState.Success(post)
                _postDetailUiState.value = PostDetailUiState.Success(post)
                addOrReplacePostInList(post)
                _eventFlow.emit(PostEvent.NavigateBack)

            }.onFailure { error ->
                val message = error.message ?: "Failed to update post."
                _postEditUiState.value = PostEditUiState.Error(
                    message
                )
                _eventFlow.emit(PostEvent.ShowSnackbar(message))
            }

            _isUploading.value = false
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            // TODO 8주차 미션: 삭제 성공 후 화면 이동 또는 Snackbar 표시를 이벤트로 분리하기
            runCatching {
                postRepository.deletePost(postId.toInt())
            }.onSuccess {
                deletedPostIds.add(postId.toInt())
                createdPosts.remove(postId.toInt())
                updatedPosts.remove(postId.toInt())
                removePostFromList(postId.toInt())

                _eventFlow.emit(PostEvent.NavigateBack)


            }.onFailure { error ->
                val message = error.message ?: "Failed to delete post."
                _postDetailUiState.value = PostDetailUiState.Error(message)
                _eventFlow.emit(PostEvent.ShowSnackbar(message))
            }
            _isUploading.value = false
        }
    }


    fun onUpdateAuthor(author: String) {
        _postCreateFormState.value = _postCreateFormState.value.copy(author = author)
    }

    fun onUpdateTitle(title: String) {
        _postCreateFormState.value = _postCreateFormState.value.copy(title = title)
    }

    fun onUpdateContent(content: String) {
        _postCreateFormState.value = _postCreateFormState.value.copy(content = content)
    }

    fun onUpdateSelectedImageUri(selectedImageUri: Uri?) {
        _postCreateFormState.value = _postCreateFormState.value.copy(
            selectedImageUri = selectedImageUri?.toString()
        )
    }

    fun onUpdateEditTitle(title: String) {
        _postEditFormState.value = _postEditFormState.value.copy(title = title)
    }

    fun onUpdateEditContent(content: String) {
        _postEditFormState.value = _postEditFormState.value.copy(body = content)
    }

    fun onUpdateEditSelectedImageUri(selectedImageUri: Uri?) = Unit

    fun onClearEditImages() = Unit

    private fun initializeEditForm(
        postId: Int,
        title: String,
        body: String,
        userId: Int,
        force: Boolean = false
    ) {
        if (!force && _postEditFormState.value.initializedPostId == postId) return

        _postEditFormState.value = PostEditFormState(
            title = title,
            body = body,
            userId = userId,
            initializedPostId = postId
        )
    }

    private fun showPostDetail(post: Post) {
        _postDetailUiState.value = PostDetailUiState.Success(post)
        _postEditUiState.value = PostEditUiState.Ready(post)
        initializeEditForm(
            postId = post.id,
            title = post.title,
            body = post.body,
            userId = post.userId
        )
    }

    private fun findLocalPost(postId: Int): Post? =
        updatedPosts[postId] ?: createdPosts[postId]

    private fun applyLocalChanges(posts: List<Post>, userId: Int?): List<Post> {
        val serverPosts = posts
            .filterNot { deletedPostIds.contains(it.id) }
            .map { post -> updatedPosts[post.id] ?: post }

        val localCreatedPosts = createdPosts.values
            .filterNot { deletedPostIds.contains(it.id) }
            .filter { userId == null || it.userId == userId }
            .map {post ->updatedPosts[post.id] ?: post}

        return (localCreatedPosts + serverPosts)
            .distinctBy { it.id }
            .sortedByDescending { it.id }
    }

    private fun addOrReplacePostInList(post: Post) {
        val currentState = uiState
        if (currentState !is PostUiState.Success) return

        val posts = currentState.posts
            .filterNot { it.id == post.id }

        _uiState.value = PostUiState.Success(
            (listOf(post) + posts)
                .filterNot { deletedPostIds.contains(it.id) }
                .sortedByDescending { it.id }
        )
    }

    private fun removePostFromList(postId: Int) {
        val currentState = uiState
        if (currentState is PostUiState.Success) {
            _uiState.value = PostUiState.Success(
                currentState.posts.filterNot { it.id == postId }
            )
        }
    }
}
