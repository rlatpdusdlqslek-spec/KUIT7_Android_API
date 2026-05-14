package com.example.kuit7th_api_practice.data.mock

import com.example.kuit7th_api_practice.data.model.request.PostCreateRequest
import com.example.kuit7th_api_practice.data.model.response.AuthorResponse
import com.example.kuit7th_api_practice.data.model.response.PostResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class InMemoryMockPostDataSource @Inject constructor() : PostLocalDataSource {
    // 서버 연동 없이 레포지토리 구현 위한 코드
    private val posts = mutableListOf(
        PostResponse(
            id = 1L,
            title = "첫 번째 실습용 게시글",
            content = "서버 연결 없이도 화면 흐름을 확인할 수 있도록 준비한 목 데이터입니다.",
            imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
            author = AuthorResponse(
                id = 1L,
                username = "anonymous",
                profileImageUrl = null
            ),
            createdAt = "2026-04-25T09:30:00",
            updatedAt = "2026-04-25T09:30:00"
        ),
        PostResponse(
            id = 2L,
            title = "Compose 게시판 UI 실습",
            content = "목록, 상세, 작성, 수정, 삭제 흐름을 하나의 ViewModel과 Hilt로 연결하는 예시입니다.",
            imageUrl = null,
            author = AuthorResponse(
                id = 2L,
                username = "kuit_member",
                profileImageUrl = null
            ),
            createdAt = "2026-04-25T10:15:00",
            updatedAt = "2026-04-25T10:15:00"
        )
    )

    private var nextId = 4L

    override suspend fun getPosts(): List<PostResponse> {
        delay(200)
        return posts.sortedByDescending { it.id }
    }

    override suspend fun getPostDetail(postId: Long): PostResponse? {
        delay(150)
        return posts.firstOrNull { it.id == postId }
    }

    override suspend fun createPost(authorName: String, request: PostCreateRequest): PostResponse {
        delay(200)

        val now = nowString()
        val post = PostResponse(
            id = nextId++,
            title = request.title,
            content = request.body,
            imageUrl = null,
            author = AuthorResponse(
                id = request.userId.toLong(),
                username = authorName.ifBlank { "anonymous" },
                profileImageUrl = null
            ),
            createdAt = now,
            updatedAt = now
        )

        posts.add(post)
        return post
    }

    override suspend fun updatePost(postId: Long, request: PostCreateRequest): PostResponse? {
        delay(200)

        val index = posts.indexOfFirst { it.id == postId }
        if (index == -1) return null

        val current = posts[index]
        val updated = current.copy(
            title = request.title,
            content = request.body,
            updatedAt = nowString()
        )

        posts[index] = updated
        return updated
    }

    override suspend fun deletePost(postId: Long): Boolean {
        delay(150)
        return posts.removeAll { it.id == postId }
    }

    private fun nowString(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}
