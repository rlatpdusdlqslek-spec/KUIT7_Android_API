package com.example.kuit7th_api_practice.ui.post.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kuit7th_api_practice.ui.post.component.PostItem
import com.example.kuit7th_api_practice.ui.post.state.PostUiState
import com.example.kuit7th_api_practice.ui.post.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    onPostClick: (Long) -> Unit,
    onCreatePostClick: () -> Unit,
    viewModel: PostViewModel
) {
    // TODO 8주차 실습: ViewModel의 화면 상태 스트림을 collect해서 Compose State로 변환하기
    // TODO 8주차 실습: 상태가 바뀌면 아래 when 구문이 자동으로 다시 그려지는지 확인하기
    val uiState = viewModel.uiState


    LaunchedEffect(Unit) {
        // TODO 8주차 실습: 화면 진입 시 Producer에서 시작된 데이터 흐름을 ViewModel에서 수집하기
        viewModel.fetchPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게시글 목록") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePostClick) {
                Icon(Icons.Default.Add, contentDescription = "게시글 작성")
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is PostUiState.Idle,
            is PostUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PostUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }

            is PostUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.posts, key = { it.id }) { post ->
                        PostItem(
                            post = post,
                            onClick = { onPostClick(post.id.toLong()) }
                        )
                    }
                }
            }
        }
    }
}
