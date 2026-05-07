package com.example.kuit7th_api_practice.data.repositoryimpl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.kuit7th_api_practice.data.model.response.PostResponse
import com.example.kuit7th_api_practice.data.repository.FavoriteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

val Context.datastore by preferencesDataStore("favorite")

class FavoriteRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context
) : FavoriteRepository {
    override suspend fun getFavorites(posts: List<PostResponse>): Map<Long, Boolean> {
        val data = context.datastore.data.first()
        return posts.associate { post ->
            val key = booleanPreferencesKey("favorite_${post.id}")
            post.id to (data[key] ?: false)
        }
    }

    override suspend fun getFavorite(id: Long): Boolean {
        val key = booleanPreferencesKey("favorite_${id}")

        return context.datastore.data.first()[key] ?: false
    }

    override suspend fun setFavorite(id: Long) {
        val key = booleanPreferencesKey("favorite_${id}")

        context.datastore.edit {
            it[key] = it[key] != true
        }
    }

}