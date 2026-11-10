package mx.edu.utez.utezify.data.models

/**
 * Interface sellada para manejo reactivo de estados UI en ViewModels
 */
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    object Empty : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
