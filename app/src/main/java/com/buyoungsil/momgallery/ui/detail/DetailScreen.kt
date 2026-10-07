package com.buyoungsil.momgallery.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.buyoungsil.momgallery.data.Artwork
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.ui.common.ArtworkImage
import com.buyoungsil.momgallery.ui.common.BigButton
import com.buyoungsil.momgallery.ui.common.CenterLoading
import com.buyoungsil.momgallery.ui.common.ErrorBox
import com.buyoungsil.momgallery.ui.common.ScreenTopBar
import com.buyoungsil.momgallery.ui.common.ZoomDialog
import com.buyoungsil.momgallery.ui.common.appViewModel
import com.buyoungsil.momgallery.ui.common.errorOrNull
import com.buyoungsil.momgallery.ui.common.valueOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailState(
    val loading: Boolean = true,
    val error: String? = null,
    val artwork: Artwork? = null,
)

class DetailViewModel(private val repo: GalleryRepository, private val id: Int) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        load()
    }

    fun imageUrl(path: String): String = repo.imageUrl(path)

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = repo.artwork(id)
            _state.update {
                DetailState(loading = false, error = result.errorOrNull(), artwork = result.valueOrNull())
            }
        }
    }
}

@Composable
fun DetailScreen(
    id: Int,
    onBack: () -> Unit,
    onInquiry: (artworkId: Int, title: String) -> Unit,
) {
    val vm = appViewModel(key = "artwork-$id") { DetailViewModel(it, id) }
    val state by vm.state.collectAsStateWithLifecycle()
    var zoom by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar("작품", onBack)

        val artwork = state.artwork
        when {
            state.loading -> CenterLoading()
            artwork == null -> ErrorBox(state.error ?: "작품을 불러오지 못했어요.", vm::load)
            else -> {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                ) {
                    ArtworkImage(
                        artwork,
                        vm.imageUrl(artwork.imageUrl),
                        Modifier
                            .fillMaxWidth()
                            .clickable { zoom = true },
                    )
                    Text(
                        "그림을 누르면 크게 볼 수 있어요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )

                    Text(
                        artwork.title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = 24.dp),
                    )

                    val rows = listOfNotNull(
                        artwork.medium.takeIf { it.isNotBlank() }?.let { "종류" to it },
                        artwork.year?.let { "제작" to "${it}년" },
                        artwork.size.takeIf { it.isNotBlank() }?.let { "크기" to it },
                        artwork.category?.let { "분류" to it.name },
                    )
                    rows.forEach { (label, value) ->
                        Text(
                            "$label   $value",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    if (artwork.description.isNotBlank()) {
                        Text(
                            artwork.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }

                    Spacer(Modifier.height(36.dp))
                    BigButton("이 작품 문의하기", { onInquiry(artwork.id, artwork.title) })
                    Spacer(Modifier.height(24.dp))
                }

                if (zoom) {
                    ZoomDialog(vm.imageUrl(artwork.imageUrl), artwork.title) { zoom = false }
                }
            }
        }
    }
}