package com.example.kuit7th_api_practice.domain.repository

import com.example.kuit7th_api_practice.domain.repository.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    // TODO 8주차 실습: Repository를 Producer로 보고, suspend 반환값을 Flow 기반 반환값으로 바꿔보기
    // TODO 8주차 실습: flow builder 안에서 API를 호출하고 emit으로 데이터를 방출하기
    suspend fun getPosts(userId: Int? = null): Flow<List<Post>>
    suspend fun getPost(id: Int): Flow<Post>
    suspend fun createPost(title: String, body: String, userId: Int): Post
    suspend fun updatePost(id: Int, title: String, body: String, userId: Int): Post
    suspend fun deletePost(id: Int)
}
