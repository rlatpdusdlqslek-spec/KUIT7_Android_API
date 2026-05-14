package com.example.kuit7th_api_practice.data.api

import com.example.kuit7th_api_practice.data.dto.PostDto
import com.example.kuit7th_api_practice.data.model.request.PostCreateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApiService {
    @GET("/posts")
    suspend fun getPosts(
        @Query("userId") userId: Int? = null
    ): List<PostDto>

    @GET("/posts/{id}")
    suspend fun getPost(
        @Path("id") id: Int
    ): PostDto

    @POST("/posts")
    suspend fun createPost(
        @Body request: PostCreateRequest
    ): PostDto

    @PATCH("/posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Body request: PostCreateRequest
    ): PostDto

    @DELETE("/posts/{id}")
    suspend fun deletePost(
        @Path("id") id: Int
    ): Response<Unit>
}
