package com.example.kuit7th_api_practice.data.repositoryimpl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kuit7th_api_practice.data.model.PostDraft
import com.example.kuit7th_api_practice.data.repository.PostDraftRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

val Context.draftDataStore by preferencesDataStore("post_draft")

class PostDraftRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PostDraftRepository {

    private object PreferencesKeys {
        val AUTHOR = stringPreferencesKey("author")
        val TITLE = stringPreferencesKey("title")
        val CONTENT = stringPreferencesKey("content")
        val IMAGE_URI = stringPreferencesKey("image_uri")
    }

    override suspend fun getDraft(): PostDraft {
        val preferences = context.draftDataStore.data.first()
        return PostDraft(
            author = preferences[PreferencesKeys.AUTHOR] ?: "",
            title = preferences[PreferencesKeys.TITLE] ?: "",
            content = preferences[PreferencesKeys.CONTENT] ?: "",
            imageUri = preferences[PreferencesKeys.IMAGE_URI]
        )
    }

    override suspend fun setAuthor(author: String) {
        context.draftDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTHOR] = author
        }
    }

    override suspend fun setTitle(title: String) {
        context.draftDataStore.edit { preferences ->
            preferences[PreferencesKeys.TITLE] = title
        }
    }

    override suspend fun setContent(content: String) {
        context.draftDataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTENT] = content
        }
    }

    override suspend fun setImageUri(uri: String?) {
        context.draftDataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(PreferencesKeys.IMAGE_URI)
            } else {
                preferences[PreferencesKeys.IMAGE_URI] = uri
            }
        }
    }

    override suspend fun clearDraft() {
        context.draftDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTHOR)
            preferences.remove(PreferencesKeys.TITLE)
            preferences.remove(PreferencesKeys.CONTENT)
            preferences.remove(PreferencesKeys.IMAGE_URI)
        }
    }
}
