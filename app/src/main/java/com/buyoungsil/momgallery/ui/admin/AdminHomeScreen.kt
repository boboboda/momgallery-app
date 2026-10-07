package com.buyoungsil.momgallery.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.ui.common.ConfirmDialog
import com.buyoungsil.momgallery.ui.common.appViewModel
import com.buyoungsil.momgallery.ui.common.valueOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminHomeViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _openCount = MutableStateFlow<Int?>(null)
    val openCount: StateFlow<Int?> = _openCount.asStateFlow()

    private val _artworkCount = MutableStateFlow<Int?>(null)
    val artworkCount: StateFlow<Int?> = _artworkCount.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _openCount.value = repo.adminInquiries(onlyOpen = true).valueOrNull()?.size
        }
        viewModelScope.launch {
            _artworkCount.value = repo.artworks().valueOrNull()?.size
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
    val artworkCount by vm.artworkCount.collectAsStateWithLifecycle()
    var askLogout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.refresh() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Text("안녕하세요, 작가님", style = MaterialTheme.typography.headlineMedium)
        Text(
            "오늘은 무엇을 할까요?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        // 가장 자주 하는 일: 크고 눈에 띄게
        Spacer(Modifier.height(28.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onNewArtwork)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.AddPhotoAlternate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "그림 올리기",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    "사진을 고르면 끝이에요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        ActionCard(
            icon = Icons.Outlined.Collections,
            title = "올린 그림",
            subtitle = artworkCount?.let { "${it}점 · 고치기, 지우기" } ?: "고치기, 지우기",
            onClick = onArtworks,
        )
        Spacer(Modifier.height(12.dp))
        val newInquiries = openCount?.takeIf { it > 0 }
        ActionCard(
            icon = Icons.Outlined.Email,
            title = "문의",
            subtitle = if (newInquiries != null) "새 문의 ${newInquiries}건이 와 있어요" else "새 문의가 없어요",
            onClick = onInquiries,
            badge = newInquiries,
        )

        Spacer(Modifier.height(36.dp))
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

// 아이콘 + 제목 + 설명이 있는 누르기 쉬운 카드
@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    badge: Int? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (badge != null) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(
                    "$badge",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
