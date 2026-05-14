package com.example.kuit7th_api_practice.ui.post.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kuit7th_api_practice.ui.post.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostEditScreen(
    postId: Long,
    onNavigateBack: () -> Unit,
    onPostUpdated: () -> Unit,
    viewModel: PostViewModel
) {
    LaunchedEffect(postId) {
        viewModel.getPostDetail(postId)
        // TODO 8주차 미션: 수정 성공 이벤트를 구독해서 뒤로가기 또는 Snackbar를 처리하기
    }

    // TODO 8주차 미션: 수정 폼 상태를 화면 상태 스트림으로 관찰하는 구조로 바꿔보기
    val title = viewModel.postEditFormState.title
    val body = viewModel.postEditFormState.body
    val isUploading = viewModel.isUploading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "게시글 수정",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.onUpdateEditTitle(it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = body,
                onValueChange = { viewModel.onUpdateEditContent(it) },
                label = { Text("Body") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                maxLines = 10,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updatePost(postId) {
                        // TODO 8주차 미션: 수정 성공 콜백을 1회성 이벤트 흐름으로 대체하기
                        onPostUpdated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() && body.isNotBlank() && !isUploading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Update",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline
)
