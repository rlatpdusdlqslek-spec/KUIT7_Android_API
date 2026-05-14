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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kuit7th_api_practice.ui.post.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreateScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: PostViewModel
) {
    // TODO 8주차 실습: 작성 폼 상태를 화면 상태 스트림으로 관찰하는 구조로 바꿔보기
    val userId = viewModel.postCreateFormState.author
    val title = viewModel.postCreateFormState.title
    val body = viewModel.postCreateFormState.content
    val isUploading = viewModel.isUploading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "게시글 작성",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                value = userId,
                onValueChange = { viewModel.onUpdateAuthor(it) },
                label = { Text("User ID") },
                placeholder = { Text("1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.onUpdateTitle(it) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = body,
                onValueChange = { viewModel.onUpdateContent(it) },
                label = { Text("Body") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                maxLines = 10,
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.createPost {
                        // TODO 8주차 실습: 작성 성공 콜백을 1회성 이벤트 흐름으로 대체하기
                        onPostCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = title.isNotBlank() && body.isNotBlank() && !isUploading,
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    "Create",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
