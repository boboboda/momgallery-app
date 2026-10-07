package com.buyoungsil.momgallery.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.momgallery.data.Artwork
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.ui.common.valueOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArtistState(
    val featured: Artwork? = null, // 대표로 걸어 둘 그림
    val count: Int = 0,
    val loading: Boolean = true, // 불러오는 동안은 액자 자리를 비워 두지 않고 빈 액자를 보여줘요
)

class ArtistViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _state = MutableStateFlow(ArtistState())
    val state: StateFlow<ArtistState> = _state.asStateFlow()

    // 로그인했는지 (로그인한 폰에서는 "작가 로그인" 버튼을 숨겨요)
    val isAdmin: StateFlow<Boolean> = repo.isAdmin

    init {
        viewModelScope.launch {
            val list = repo.artworks().valueOrNull().orEmpty()
            _state.update {
                ArtistState(
                    featured = list.firstOrNull { a -> a.featured } ?: list.firstOrNull(),
                    count = list.size,
                    loading = false,
                )
            }
        }
    }

    fun imageUrl(path: String): String = repo.imageUrl(path)
}
