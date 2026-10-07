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
)

class ArtistViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _state = MutableStateFlow(ArtistState())
    val state: StateFlow<ArtistState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val list = repo.artworks().valueOrNull().orEmpty()
            _state.update {
                ArtistState(
                    featured = list.firstOrNull { a -> a.featured } ?: list.firstOrNull(),
                    count = list.size,
                )
            }
        }
    }

    fun imageUrl(path: String): String = repo.imageUrl(path)
}