package com.buyoungsil.momgallery.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.ui.common.BigButton
import com.buyoungsil.momgallery.ui.common.ConfirmDialog
import com.buyoungsil.momgallery.ui.common.ScreenTopBar
import com.buyoungsil.momgallery.ui.common.appViewModel
import com.buyoungsil.momgallery.ui.common.valueOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminHomeViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _openCount = MutableStateFlow<Int?>(null)
    val openCount: StateFlow<Int?> = _openCount.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _openCount.value = repo.adminInquiries(onlyOpen = true).valueOrNull()?.size
        }
    }

    fun logout() {
        viewModelScope.launch { repo.logout() }
    }
}

@Composable
fun AdminHomeScreen(
    onNewArtwork: () -> Unit,
    onArtworks: () -> Unit,
    onInquiries: () -> Unit,
) {
    val vm = appViewModel { AdminHomeViewModel(it) }
    val openCount by vm.openCount.collectAsStateWithLifecycle()
    var askLogout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refresh() }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar("내 그림 관리")

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text("안녕하세요", style = MaterialTheme.typography.headlineMedium)
            Text(
                "오늘은 무엇을 할까요?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )

            Spacer(Modifier.height(32.dp))
            BigButton("그림 올리기", onNewArtwork)
            Spacer(Modifier.height(16.dp))
            BigButton("올린 그림 고치기 · 지우기", onArtworks, outlined = true)
            Spacer(Modifier.height(16.dp))
            BigButton(
                text = openCount?.takeIf { it > 0 }?.let { "문의 보기  (새 문의 ${it}건)" } ?: "문의 보기",
                onClick = onInquiries,
                outlined = true,
            )

            Spacer(Modifier.height(40.dp))
            TextButton(
                onClick = { askLogout = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Text(
                    "로그아웃",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (askLogout) {
        ConfirmDialog(
            title = "로그아웃할까요?",
            message = "로그아웃하면 다음에 다시 로그인해야 해요.",
            confirmText = "로그아웃",
            onConfirm = {
                askLogout = false
                vm.logout()
            },
            onDismiss = { askLogout = false },
        )
    }
}
