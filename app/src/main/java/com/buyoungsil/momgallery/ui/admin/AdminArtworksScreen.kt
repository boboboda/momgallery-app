package com.buyoungsil.momgallery.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.buyoungsil.momgallery.data.Artwork
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.ui.common.BigButton
import com.buyoungsil.momgallery.ui.common.CenterLoading
import com.buyoungsil.momgallery.ui.common.ErrorBox
import com.buyoungsil.momgallery.ui.common.ScreenTopBar
import com.buyoungsil.momgallery.ui.common.appViewModel
import com.buyoungsil.momgallery.ui.common.errorOrNull
import com.buyoungsil.momgallery.ui.common.subtitle
import com.buyoungsil.momgallery.ui.common.valueOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminArtworksState(
    val loading: Boolean = true,
    val error: String? = null,
    val items: List<Artwork> = emptyList(),
)

class AdminArtworksViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _state = MutableStateFlow(AdminArtworksState())
    val state: StateFlow<AdminArtworksState> = _state.asStateFlow()

    fun imageUrl(path: String): String = repo.imageUrl(path)

    fun load() {
        viewModelScope.launch {
            if (_state.value.items.isEmpty()) _state.update { it.copy(loading = true) }
            val r = repo.artworks()
            _state.update {
                AdminArtworksState(
                    loading = false,
                    error = r.errorOrNull(),
                    items = r.valueOrNull() ?: it.items,
                )
            }
        }
    }
}

@Composable
fun AdminArtworksScreen(onBack: () -> Unit, onNew: () -> Unit, onEdit: (Int) -> Unit) {
    val vm = appViewModel { AdminArtworksViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

    // 고치고 돌아왔을 때 목록을 다시 불러와요
    LaunchedEffect(Unit) { vm.load() }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar("올린 그림", onBack)

        when {
            state.loading && state.items.isEmpty() -> CenterLoading()
            state.error != null && state.items.isEmpty() -> ErrorBox(state.error!!, vm::load)
            else -> LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { BigButton("새 그림 올리기", onNew) }

                if (state.items.isEmpty()) {
                    item {
                        Text(
                            "아직 올린 그림이 없어요.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }

                items(state.items, key = { it.id }) { artwork ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onEdit(artwork.id) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AsyncImage(
                            model = vm.imageUrl(artwork.imageUrl),
                            contentDescription = artwork.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(96.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(artwork.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                artwork.subtitle(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (artwork.featured) {
                                Text(
                                    "첫 화면에 걸려 있어요",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Text(
                            "고치기",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
