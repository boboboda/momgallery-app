package com.buyoungsil.momgallery.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buyoungsil.momgallery.data.Artwork
import com.buyoungsil.momgallery.data.Category
import com.buyoungsil.momgallery.data.GalleryRepository
import com.buyoungsil.momgallery.ui.common.errorOrNull
import com.buyoungsil.momgallery.ui.common.valueOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val loading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val featured: List<Artwork> = emptyList(),
    val artworks: List<Artwork> = emptyList(),
    val selectedCategory: Int? = null,
)

class HomeViewModel(private val repo: GalleryRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        load()
    }

    fun imageUrl(path: String): String = repo.imageUrl(path)

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val cats = repo.categories()
            val feat = repo.featured()
            val all = repo.artworks(_state.value.selectedCategory)
            val error = cats.errorOrNull() ?: feat.errorOrNull() ?: all.errorOrNull()
            _state.update {
                it.copy(
                    loading = false,
                    error = error,
                    categories = cats.valueOrNull() ?: it.categories,
                    featured = feat.valueOrNull() ?: it.featured,
                    artworks = all.valueOrNull() ?: it.artworks,
                )
            }
        }
    }

    fun select(categoryId: Int?) {
        if (categoryId == _state.value.selectedCategory) return
        _state.update { it.copy(selectedCategory = categoryId) }
        viewModelScope.launch {
            val result = repo.artworks(categoryId)
            _state.update { it.copy(artworks = result.valueOrNull() ?: it.artworks, error = result.errorOrNull()) }
        }
    }
}