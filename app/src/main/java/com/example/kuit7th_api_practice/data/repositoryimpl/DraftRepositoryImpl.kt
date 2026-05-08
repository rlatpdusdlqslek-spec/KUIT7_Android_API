package com.example.kuit7th_api_practice.data.repositoryimpl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.kuit7th_api_practice.data.repository.DraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DraftRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DraftRepository {

    private object PreferencesKeys {
        val AUTHOR = stringPreferencesKey("author")
        val TITLE = stringPreferencesKey("title")
        val CONTENT = stringPreferencesKey("content")
    }

    override fun getAuthor(): Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTHOR] ?: ""
    }

    override fun getTitle(): Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TITLE] ?: ""
    }

    override fun getContent(): Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CONTENT] ?: ""
    }

    override suspend fun saveAuthor(author: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTHOR] = author
        }
    }

    override suspend fun saveTitle(title: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TITLE] = title
        }
    }

    override suspend fun saveContent(content: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTENT] = content
        }
    }

    override suspend fun clearDraft() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTHOR)
            preferences.remove(PreferencesKeys.TITLE)
            preferences.remove(PreferencesKeys.CONTENT)
        }
    }
}
