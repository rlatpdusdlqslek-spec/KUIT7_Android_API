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
import com.example.kuit7th_api_practice.ui.post.state.PostUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    // TODO 8주차 실습: mutableStateOf 상태를 ViewModel 내부 수정용 상태와 UI 공개용 상태로 분리하기
    // TODO 8주차 실습: UI는 읽기만 가능하고 ViewModel만 상태를 변경할 수 있도록 구조 바꾸기
    var uiState by mutableStateOf<PostUiState>(PostUiState.Idle)
        private set

    // TODO 8주차 미션: 상세 화면 상태도 같은 방식으로 화면 상태 스트림으로 관리하기
    var postDetailUiState by mutableStateOf<PostDetailUiState>(PostDetailUiState.Loading)
        private set

    // TODO 8주차 실습, 미션: 작성/수정 성공 후 화면 이동 같은 1회성 동작은 상태가 아니라 이벤트로 분리하기
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

    private val createdPosts = mutableMapOf<Int, Post>()
    private val updatedPosts = mutableMapOf<Int, Post>()
    private val deletedPostIds = mutableSetOf<Int>()

    fun fetchPosts(userId: Int? = null) {
        // TODO 8주차 실습: Loading 상태를 UI 상태 스트림에 반영하기
        uiState = PostUiState.Loading

        viewModelScope.launch {
            // TODO 8주차 실습: Repository의 Flow를 collect해서 Success 상태로 변환하기
            // TODO 8주차 실습: catch 또는 runCatching 위치를 비교하며 에러 처리 흐름 이해하기
            runCatching {
                postRepository.getPosts(userId)
            }.onSuccess { posts ->
                uiState = PostUiState.Success(applyLocalChanges(posts, userId))
            }.onFailure { error ->
                uiState = PostUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun getPostDetail(postId: Long) {
        // TODO 8주차 미션: 상세 상태도 Loading, Success, Error 흐름으로 관리하기
        postDetailUiState = PostDetailUiState.Loading
        postEditUiState = PostEditUiState.Loading

        val localPost = findLocalPost(postId.toInt())
        if (localPost != null) {
            showPostDetail(localPost)
            return
        }

        if (deletedPostIds.contains(postId.toInt())) {
            val message = "Deleted post."
            postDetailUiState = PostDetailUiState.Error(message)
            postEditUiState = PostEditUiState.Error(message)
            return
        }

        viewModelScope.launch {
            runCatching {
                postRepository.getPost(postId.toInt())
            }.onSuccess { post ->
                showPostDetail(post)
            }.onFailure { error ->
                val message = error.message ?: "Failed to load post."
                postDetailUiState = PostDetailUiState.Error(message)
                postEditUiState = PostEditUiState.Error(message)
            }
        }
    }

    fun createPost(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isUploading = true
            postCreateUiState = PostCreateUiState.Saving

            // TODO 8주차 실습: onSuccess 콜백 대신 저장 완료 이벤트를 방출하도록 바꾸기
            // TODO 8주차 실습: Toast, Snackbar, Navigation처럼 한 번만 처리할 동작을 이벤트로 분리하기
            runCatching {
                postRepository.createPost(
                    title = postCreateFormState.title,
                    body = postCreateFormState.content,
                    userId = postCreateFormState.author.toIntOrNull() ?: 1
                )
            }.onSuccess { post ->
                createdPosts[post.id] = post
                postCreateUiState = PostCreateUiState.Success(post)
                postCreateFormState = PostCreateFormState()
                addOrReplacePostInList(post)
                onSuccess()
            }.onFailure { error ->
                postCreateUiState = PostCreateUiState.Error(
                    error.message ?: "Failed to create post."
                )
            }

            isUploading = false
        }
    }

    fun updatePost(postId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isUploading = true
            postEditUiState = PostEditUiState.Saving

            // TODO 8주차 미션: 수정 성공 후 뒤로가기 처리를 1회성 이벤트로 분리하기
            runCatching {
                postRepository.updatePost(
                    id = postId.toInt(),
                    title = postEditFormState.title,
                    body = postEditFormState.body,
                    userId = postEditFormState.userId
                )
            }.onSuccess { post ->
                updatedPosts[post.id] = post
                postEditUiState = PostEditUiState.Success(post)
                postDetailUiState = PostDetailUiState.Success(post)
                addOrReplacePostInList(post)
                onSuccess()
            }.onFailure { error ->
                postEditUiState = PostEditUiState.Error(
                    error.message ?: "Failed to update post."
                )
            }

            isUploading = false
        }
    }

    fun deletePost(postId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO 8주차 미션: 삭제 성공 후 화면 이동 또는 Snackbar 표시를 이벤트로 분리하기
            runCatching {
                postRepository.deletePost(postId.toInt())
            }.onSuccess {
                deletedPostIds.add(postId.toInt())
                createdPosts.remove(postId.toInt())
                updatedPosts.remove(postId.toInt())
                removePostFromList(postId.toInt())
                onSuccess()
            }.onFailure { error ->
                postDetailUiState = PostDetailUiState.Error(
                    error.message ?: "Failed to delete post."
                )
            }
        }
    }

    fun onUpdateAuthor(author: String) {
        postCreateFormState = postCreateFormState.copy(author = author)
    }

    fun onUpdateTitle(title: String) {
        postCreateFormState = postCreateFormState.copy(title = title)
    }

    fun onUpdateContent(content: String) {
        postCreateFormState = postCreateFormState.copy(content = content)
    }

    fun onUpdateSelectedImageUri(selectedImageUri: Uri?) {
        postCreateFormState = postCreateFormState.copy(
            selectedImageUri = selectedImageUri?.toString()
        )
    }

    fun onUpdateEditTitle(title: String) {
        postEditFormState = postEditFormState.copy(title = title)
    }

    fun onUpdateEditContent(content: String) {
        postEditFormState = postEditFormState.copy(body = content)
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
        if (!force && postEditFormState.initializedPostId == postId) return

        postEditFormState = PostEditFormState(
            title = title,
            body = body,
            userId = userId,
            initializedPostId = postId
        )
    }

    private fun showPostDetail(post: Post) {
        postDetailUiState = PostDetailUiState.Success(post)
        postEditUiState = PostEditUiState.Ready(post)
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

        return (localCreatedPosts + serverPosts)
            .distinctBy { it.id }
            .sortedByDescending { it.id }
    }

    private fun addOrReplacePostInList(post: Post) {
        val currentState = uiState
        if (currentState !is PostUiState.Success) return

        val posts = currentState.posts
            .filterNot { it.id == post.id }

        uiState = PostUiState.Success(
            (listOf(post) + posts)
                .filterNot { deletedPostIds.contains(it.id) }
                .sortedByDescending { it.id }
        )
    }

    private fun removePostFromList(postId: Int) {
        val currentState = uiState
        if (currentState is PostUiState.Success) {
            uiState = PostUiState.Success(
                currentState.posts.filterNot { it.id == postId }
            )
        }
    }
}
