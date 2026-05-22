package com.example.kuit7th_api_practice.data.repositoryimpl

import com.example.kuit7th_api_practice.data.api.PostApiService
import com.example.kuit7th_api_practice.data.dto.toDomain
import com.example.kuit7th_api_practice.data.model.request.PostCreateRequest
import com.example.kuit7th_api_practice.domain.repository.PostRepository
import com.example.kuit7th_api_practice.domain.repository.model.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postService: PostApiService
) : PostRepository {
    // TODO 8주차 실습: API 응답을 Flow로 감싸서 Producer 역할을 하도록 수정하기
    // TODO 8주차 실습: DTO를 Domain으로 변환하는 map 처리를 Flow 흐름 안에서 다뤄보기
    override suspend fun getPosts(userId: Int?): Flow<List<Post>> = flow {
        emit(value=postService.getPosts(userId).map { it.toDomain() })
    }



    override suspend fun getPost(id: Int): Flow<Post> = flow{
        emit(postService.getPost(id).toDomain())}

    override suspend fun createPost(title: String, body: String, userId: Int): Post =
        postService.createPost(
            PostCreateRequest(
                title = title,
                body = body,
                userId = userId
            )
        ).toDomain()

    override suspend fun updatePost(id: Int, title: String, body: String, userId: Int): Post =
        postService.updatePost(
            id = id,
            request = PostCreateRequest(
                title = title,
                body = body,
                userId = userId
            )
        ).toDomain(fallId =id)

    override suspend fun deletePost(id: Int) {
        postService.deletePost(id)
    }
}
