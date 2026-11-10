package mx.edu.utez.utezify.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utez.utezify.data.models.Song
import mx.edu.utez.utezify.data.models.SongHistoryResponse
import mx.edu.utez.utezify.data.models.UiState
import mx.edu.utez.utezify.data.repository.SongRepository

class SongViewModel(
    private val songRepository: SongRepository
) : ViewModel() {

    private val _songsState = MutableStateFlow<UiState<List<Song>>>(UiState.Loading)
    val songsState: StateFlow<UiState<List<Song>>> = _songsState.asStateFlow()

    private val _operationState = MutableStateFlow<UiState<String>>(UiState.Empty)
    val operationState: StateFlow<UiState<String>> = _operationState.asStateFlow()

    private val _historyState = MutableStateFlow<UiState<SongHistoryResponse>>(UiState.Empty)
    val historyState: StateFlow<UiState<SongHistoryResponse>> = _historyState.asStateFlow()

    init {
        fetchSongs()
    }

    fun fetchSongs() {
        viewModelScope.launch {
            _songsState.value = UiState.Loading
            val result = songRepository.getSongs()
            result.onSuccess { songs ->
                if (songs.isEmpty()) {
                    _songsState.value = UiState.Empty
                } else {
                    _songsState.value = UiState.Success(songs)
                }
            }.onFailure { error ->
                _songsState.value = UiState.Error(error.localizedMessage ?: "Error al cargar canciones")
            }
        }
    }

    /**
     * Consulta el historial de modificaciones (`song_update_history` y `song_deletion_history`) de una canción.
     */
    fun fetchSongHistory(songId: Int) {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            val result = songRepository.getSongHistory(songId)
            result.onSuccess { history ->
                _historyState.value = UiState.Success(history)
            }.onFailure { error ->
                _historyState.value = UiState.Error(error.localizedMessage ?: "Error al obtener historial de la canción")
            }
        }
    }

    fun clearHistoryState() {
        _historyState.value = UiState.Empty
    }

    /**
     * Procesa la lectura del Tag NFC buscando la primera canción del alumno en el servidor.
     */
    fun processNfcScan(
        matricula: String,
        onPlaySong: (Song) -> Unit,
        onNoSongsFound: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = songRepository.getFirstSongByUser(matricula)
            result.onSuccess { song ->
                onPlaySong(song)
            }.onFailure { error ->
                onNoSongsFound(error.localizedMessage ?: "No se encontraron canciones para la matrícula: $matricula")
            }
        }
    }

    /**
     * Sube una nueva canción exigiendo los archivos de audio e imagen.
     */
    fun uploadSong(
        context: Context,
        title: String,
        artist: String,
        duration: String,
        audioUri: Uri,
        imageUri: Uri,
        createdBy: String
    ) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = songRepository.uploadSong(
                context = context,
                title = title,
                artist = artist,
                duration = duration,
                audioUri = audioUri,
                imageUri = imageUri,
                createdBy = createdBy
            )
            result.onSuccess {
                _operationState.value = UiState.Success("Canción registrada correctamente")
                fetchSongs()
            }.onFailure { error ->
                _operationState.value = UiState.Error(error.localizedMessage ?: "Error al subir la canción")
            }
        }
    }

    /**
     * Actualiza los metadatos y/o reemplaza archivos de una canción existente.
     */
    fun updateSong(
        context: Context,
        songId: Int,
        title: String,
        artist: String,
        duration: String,
        audioUri: Uri?,
        imageUri: Uri?,
        updatedBy: String
    ) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = songRepository.updateSong(
                context = context,
                songId = songId,
                title = title,
                artist = artist,
                duration = duration,
                audioUri = audioUri,
                imageUri = imageUri,
                updatedBy = updatedBy
            )
            result.onSuccess {
                _operationState.value = UiState.Success("Canción actualizada correctamente")
                fetchSongs()
            }.onFailure { error ->
                _operationState.value = UiState.Error(error.localizedMessage ?: "Error al actualizar la canción")
            }
        }
    }

    /**
     * Elimina lógicamente una canción en el backend.
     */
    fun softDeleteSong(songId: Int, deletedBy: String) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            val result = songRepository.softDeleteSong(songId, deletedBy)
            result.onSuccess {
                _operationState.value = UiState.Success("Canción eliminada correctamente")
                fetchSongs()
            }.onFailure { error ->
                _operationState.value = UiState.Error(error.localizedMessage ?: "Error al eliminar la canción")
            }
        }
    }

    fun clearOperationState() {
        _operationState.value = UiState.Empty
    }
}
