package com.chading.photos.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chading.photos.data.Album
import com.chading.photos.data.MediaItem
import com.chading.photos.data.MediaStoreRepository
import com.chading.photos.data.toAlbums
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class MediaUiState(
    val isLoading: Boolean = true,
    val items: List<MediaItem> = emptyList(),
    val albums: List<Album> = emptyList(),
    val errorMessage: String? = null,
)

class PhotosViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MediaStoreRepository(app)

    private val _state = MutableStateFlow(MediaUiState())
    val state: StateFlow<MediaUiState> = _state.asStateFlow()

    private var started = false

    fun onPermissionGranted() {
        if (started) return
        started = true
        viewModelScope.launch {
            repo.observeMedia()
                .catch { e ->
                    _state.value = _state.value.copy(isLoading = false, errorMessage = e.message)
                }
                .collect { items ->
                    _state.value = MediaUiState(
                        isLoading = false,
                        items = items,
                        albums = items.toAlbums(),
                    )
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                PhotosViewModel(app)
            }
        }
    }
}
